package microgram.impl.rest.replication;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

import microgram.api.java.Result;
import microgram.impl.rest.replication.kafka.KafkaClient;
import utils.Queues;

public class OrderedExecutor {

	private static final String DEFAULT_KEY = "_";
	
	final KafkaClient kafka;
	final MicrogramTopic topic;
	final Map<Object, BlockingQueue<Result<?>>> queues;

	public OrderedExecutor(MicrogramTopic topic, int partitions) {
		this.topic = topic;
		this.kafka = new KafkaClient();

		kafka.createTopic(topic, partitions);
		this.queues = new ConcurrentHashMap<>();
	}

	public OrderedExecutor init(MicrogramOperationExecutor executor) {
		kafka.subscribe((t, k, v, ko) -> {
			System.err.printf("%s %s %s - %d\n", k, v, ko, System.currentTimeMillis());
			
			MicrogramOperation op = new MicrogramOperation(v);

			Result<?> result = executor.execute(op);

			BlockingQueue<Result<?>> q = queues.remove(op.id);
			if (q != null)
				Queues.putInto(q, result);

		}, topic);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> Result<T> replicate(MicrogramOperation op) {
		try {
			BlockingQueue<Result<?>> q;

			queues.put(op.id, q = new SynchronousQueue<>());

			kafka.publish(topic, DEFAULT_KEY, op.encode());

			return (Result<T>) Queues.takeFrom(q);
		} finally {
		}
	}
}
