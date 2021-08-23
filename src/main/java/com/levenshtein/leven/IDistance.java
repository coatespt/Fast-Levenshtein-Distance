package com.levenshtein.leven;

/**
 * Contract for a LD Distance compute object.  It's just one method--compute the LD of two strings.
 * This is heavily used in tests.
 *
 * It is an interface because we may want various LD implementations.
 *
 * TODO: This should be templated so that it could handle sequences of arbitrary type.
 * 
 * @author pcoates
 *
 */
public interface IDistance {
	int LD(String s1, String s2);
}
