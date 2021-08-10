package a140.util;

/**
 * @author petercoates
 * 
 */
public class LongIntPair implements Comparable<Object> {
	protected long key;
	protected int ct;

	public LongIntPair() {
	}

	public LongIntPair(long l, int v) {
		this.key = l;
		this.ct = v;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("LongIntPair:[");
		sb.append(key);
		sb.append(",");
		sb.append(ct);
		sb.append("]");
		return sb.toString();
	}

	public int compareTo(Object o) {
		if (!(o instanceof LongIntPair)) {
			throw new ClassCastException("Invalid object");
		}
		LongIntPair other = ((LongIntPair) o);
		if (this.getCt() > other.getCt()) {
			return 1;
		} else if (this.getCt() < other.getCt()) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * @return the str
	 */
	public long getLong() {
		return key;
	}

	/**
	 * @param str
	 *            the str to set
	 */
	public void setLong(long key) {
		this.key = key;
	}

	/**
	 * @return the ct
	 */
	public Integer getCt() {
		return ct;
	}

	/**
	 * @param ct
	 *            the ct to set
	 */
	public void setCt(Integer ct) {
		this.ct = ct;
	}
}