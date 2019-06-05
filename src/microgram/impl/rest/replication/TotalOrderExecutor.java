package microgram.impl.rest.replication;

public class TotalOrderExecutor extends OrderedExecutor {
	
	private static final int TOPIC_PARTITIONS = 1; // must be 1 partition for total order delivery...

	public TotalOrderExecutor(MicrogramTopic topic) {
		super( topic, TOPIC_PARTITIONS );
	}	
}
