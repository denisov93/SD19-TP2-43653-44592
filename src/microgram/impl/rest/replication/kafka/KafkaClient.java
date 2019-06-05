package microgram.impl.rest.replication.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import microgram.impl.rest.replication.MicrogramTopic;
import utils.Random;

public class KafkaClient {
	static final String KAFKA_BROKERS = "kafka1:9092,kafka2:9092,kafka3:9092";

	public static void clean() {
		KafkaUtils.clean();
	}
	
	public void deleteTopic(String topic) {
		KafkaUtils.deleteTopic( topic );
	}
	
	public void createTopic(MicrogramTopic topic, int... partitions) {
		
		KafkaUtils.createTopic(topic.name(), partitions.length == 0 ? 1 : partitions[0], 1);
	}

	public <T> void subscribe(MicrogramEventHandler handler, MicrogramTopic... topics) {
		List<String> topicNames = Arrays.asList(topics).stream().map(MicrogramTopic::name).collect(Collectors.toList());
		new Subscriber(topicNames).consume((topic, key, value, o) -> {
			handler.onMicrogramEvent(MicrogramTopic.valueOf(topic), key, value, o);
		});
	}

	public KafkaOrder publish(MicrogramTopic topic, String key, String data) {
		return publisher().send(topic.name(), key, data);
	}

	private synchronized Publisher publisher() {
		if (publisher == null)
			publisher = new Publisher().init();
		return publisher;
	}

	private Publisher publisher;

	public static interface MicrogramEventHandler {
		void onMicrogramEvent(MicrogramTopic topic, String key, String value, KafkaOrder ko);
	}
}

class Publisher {

	private Producer<String, String> producer;

	public Publisher() {
	}

	public Publisher init() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaClient.KAFKA_BROKERS);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producer = new KafkaProducer<>(config);
		return this;
	}

	public KafkaOrder send(String topic, String key, String value) {
		try {
			RecordMetadata r = producer.send(new ProducerRecord<>(topic, key, value)).get();
			return new KafkaOrder(r.partition(), r.offset());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return new KafkaOrder(-1, -1);
		}
	}
}

class Subscriber {

	private final List<String> topics;

	public Subscriber(List<String> topics) {
		this.topics = topics;
	}

	public void consume(Listener listener) {

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaClient.KAFKA_BROKERS);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, Random.key128());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Random.key128());

		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		new Thread(() -> {
			try (Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
				consumer.subscribe(topics);
				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
					records.forEach(r -> {
						listener.onReceive(r.topic(), r.key(), r.value(), new KafkaOrder(r.partition(), r.offset()));
					});
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}).start();
	}

	static public interface Listener {
		void onReceive(String topic, String key, String value, KafkaOrder ko);
	}
}

class KafkaUtils {

	private static final String ZK_TOPICS_PATH1 = "/brokers/topics/";
	private static final String ZK_TOPICS_PATH2 = "/admin/delete_topics/";

	public static void clean() {

		try (AdminClient client = create()) {

			List<String> topics = client.listTopics().listings().get().stream().map(TopicListing::name).collect(Collectors.toList());

			topics.forEach( KafkaUtils::deleteTopic);

		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void deleteTopic(String topic) {
		try (AdminClient client = create()) {
			
			client.deleteTopics( Arrays.asList( topic ) ).all().get();
			
			Zookeeper zoo = new Zookeeper(KafkaClient.KAFKA_BROKERS.replace("9092", "2181"));
			zoo.delete(ZK_TOPICS_PATH1 + topic );
			zoo.delete(ZK_TOPICS_PATH2 + topic );
			
		} catch (ExecutionException x) {
			if (x.getCause() instanceof TopicExistsException)
				System.err.printf("Topic: %s already exists...\n", topic);
			else
				x.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createTopic(String topic, int numPartitions, int replicationFactor) {
		try (AdminClient client = create()) {
			
			client.createTopics(Arrays.asList(new NewTopic(topic, numPartitions, (short) replicationFactor))).all().get();						
		} catch (ExecutionException x) {
			if (x.getCause() instanceof TopicExistsException)
				System.err.printf("Topic: %s already exists...\n", topic);
			else
				x.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static private AdminClient create() {
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaClient.KAFKA_BROKERS);
		props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
		return AdminClient.create(props);
	}
}

//Needed to be able to fully delete topics...
class Zookeeper {
	
	private ZooKeeper zookeeper;
	private static final int TIMEOUT = 5000;
	private CountDownLatch connectedSignal = new CountDownLatch(1);

	final String servers;

	public Zookeeper(String servers) throws Exception {
		this.servers = servers;
	}

	public void close() {
		try {
			getZooKeeper().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ZooKeeper getZooKeeper() throws Exception {
		while (zookeeper == null || !zookeeper.getState().equals(ZooKeeper.States.CONNECTED)) {
			connect();
			Thread.sleep(1000);
		}
		return zookeeper;
	}

	private void connect() throws IOException, InterruptedException {
		Logger.getAnonymousLogger().log(Level.INFO, "Estabelecendo ligação ao zookeeper em: " + servers);
		zookeeper = new ZooKeeper(servers, TIMEOUT, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
					connectedSignal.countDown();
				}
			}
		});
		connectedSignal.await();
	}

	public void delete(String path) {
		try {
			Stat stat = getZooKeeper().exists(path, false);
			if( stat != null )
				getZooKeeper().delete(path, stat.getVersion() );
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}

