/**
 * 
 */
package a140.util;

/**
 * @author peter
 * 
 */
public class StringIntPair implements Comparable<Object> {
	protected String str;
	protected Integer ct;

	public StringIntPair(String str, Integer ct) {
		this.str = str;
		this.ct = ct;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StringIntPair:[");
		sb.append(str);
		sb.append(",");
		sb.append(ct);
		sb.append("]");
		return sb.toString();
	}

	public int compareTo(Object o) {
		if (!(o instanceof StringIntPair)) {
			throw new ClassCastException("Invalid object");
		}
		StringIntPair other = ((StringIntPair) o);
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
	public String getStr() {
		return str;
	}

	/**
	 * @param str
	 *            the str to set
	 */
	public void setStr(String str) {
		this.str = str;
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