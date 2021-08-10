package a140.util;

/**
 * @author peter
 * 
 */
public class IntIntPair implements Comparable<Object> {
	private Integer key;
	private Integer val;

	public IntIntPair(Integer key, Integer ct) {
		this.key = key;
		this.val = ct;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("IntIntPair:[");
		sb.append(key);
		sb.append(",");
		sb.append(val);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof IntIntPair)) {
			throw new ClassCastException("Invalid object");
		}
		IntIntPair other = ((IntIntPair) o);
		if (this.getVal() > other.getVal()) {
			return 1;
		} else if (this.getVal() < other.getVal()) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * @return the str
	 */
	public Integer getKey() {
		return key;
	}

	/**
	 * @param str
	 *            the str to set
	 */
	public void setKey(Integer key) {
		this.key = key;
	}

	/**
	 * @return the ct
	 */
	public Integer getVal() {
		return val;
	}

	/**
	 * @param ct
	 *            the ct to set
	 */
	public void setCt(Integer ct) {
		this.val = ct;
	}
}