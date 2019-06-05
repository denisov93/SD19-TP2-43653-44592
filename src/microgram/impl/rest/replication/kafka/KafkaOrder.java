package microgram.impl.rest.replication.kafka;

public class KafkaOrder implements Comparable<KafkaOrder> {
	
	public final long offset;
	public final int partition;
	
	KafkaOrder(int partition, long offset ) {
		this.partition = partition; this.offset = offset;
	}
	
	@Override
	public int hashCode() {
		return (int)(partition ^ (offset >>> 32) ^ (offset & 0xFFFFFFFFL));
	}
	
	public boolean equals( KafkaOrder other ) {
		return this.partition == other.partition && this.offset == other.offset;
	}
	
	@Override
	public boolean equals( Object other ) {
		return other != null && 
				this.offset == ((KafkaOrder)other).offset &&
				this.partition == ((KafkaOrder)other).partition;
	}
	
	@Override
	public int compareTo(KafkaOrder other) {
		if( this.partition == other.partition )
			return Long.compare(this.offset, other.offset);
		else
			return this.partition < other.partition ? -1: 1 ;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", partition, offset);
	}
}