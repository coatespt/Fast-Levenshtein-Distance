package a140;

import gnu.trove.map.hash.TLongIntHashMap;

import java.util.Random;

public class StringToIntHashMap {
	private TLongIntHashMap tMap = null;
	private static final long[] byteTable = createLookupTable();
	private static final long HSTART = 0xBB40E64DA205B064L;
	private static final long HMULT = 7664345821815920749L;

	public StringToIntHashMap() {
		tMap = new TLongIntHashMap();
	}

	public StringToIntHashMap(int size) {
		tMap = new TLongIntHashMap(size);
	}

	public void put(String key, int value) {
		tMap.put(hash(key), value);
	}

	public void put(long key, int value) {
		tMap.put(key, value);
	}

	public void clear() {
		tMap.clear();
	}

	/**
	 * Get the value for the original string key.
	 * 
	 * @param key
	 * @return
	 */
	public int get(String key) {
		return tMap.get(hash(key));
	}

	/**
	 * Get the value for the key already in its hashed-to-long version
	 * 
	 * @param key
	 * @return
	 */
	public int get(long key) {
		return tMap.get(key);
	}

	/**
	 * Answer whether the underlying lont-to-int table contains a long key that
	 * corresponds to the given string.
	 * 
	 * @param key
	 *            A string (word)
	 * @return True or false, as the underlying table contains an entry for this
	 *         word
	 */
	public boolean containsKey(String key) {
		return tMap.contains(hash(key));
	}

	/**
	 * Answers whether the underlying map contains the given long value. The
	 * value is assumed to be the pre-hashed string. This is an optimization to
	 * spare the need for N hashes of the same value when N of these tables (for
	 * the partitions) are checked for the same word.
	 * 
	 * @param hashVal
	 *            A long corresponding to a string key (word)
	 * @return True or false, as the underlying table contains an entry for this
	 *         word
	 * @return
	 */
	public boolean containsKey(long hashVal) {
		return tMap.contains(hashVal);
	}

	public int size() {
		return tMap.size();
	}

	public long[] keySet() {
		return tMap.keys();
	}

	/**
	 * Add a value to the counter specified by the key. Return the current sum
	 * of all counters with the total added to it.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public void accumulate(String key, int val) {
		int currCt = get(key);
		put(key, currCt + val);
	}

	/**
	 * Compute the hash that would be used internally to turn a string into a
	 * long.
	 * 
	 * @param key
	 * @return
	 */
	public static long hash(String key) {
		byte bytes[] = getBytes(key);
		long h = hash(bytes);
		return h;
	}

	public static byte[] getBytes(String str) {
		byte[] defaultBytes = null;
		defaultBytes = str.getBytes();
		return defaultBytes;
	}

	public static long hash(byte[] data) {
		long h = HSTART;
		final long hmult = HMULT;
		final long[] ht = byteTable;
		for (int len = data.length, i = 0; i < len; i++) {
			h = (h * hmult) ^ ht[data[i] & 0xff];
		}
		return h;
	}

	private static long[] createLookupTable() {
		long[] table = new long[256];
		Random rand = new Random(12345677);
		for (int i = 0; i < 256; i++) {
			table[i] = rand.nextLong();
		}
		return table;
	}
}
