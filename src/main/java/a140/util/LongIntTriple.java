package a140.util;

/**
 * Tacks an extra value onto the LongIntPair to hold the partition number.
 * Continues to be sorted by count.
 * 
 * @author petercoates
 */
public class LongIntTriple extends LongIntPair {

	private int partition;

	public LongIntTriple(long key, int val, int index) {
		super(key, val);
		this.setPartition(index);
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("LongIntPair:[");
		sb.append(key);
		sb.append(",");
		sb.append(ct);
		sb.append(",");
		sb.append(partition);
		sb.append("]");
		return sb.toString();
	}

}
