package com.levenshtein.leven;

/**
 * Method one:
 * <p>
 * Bodies of text have different statistical properties---entropy, how characters are grouped, the frequency of 
 * different characters, etc.
 * For instance, tab-delimited files have many more delimiters and escape characters than plain text. Etc.
 * HTML has many more curly brackets and forward slash characters than normal text.
 * <p>
 * This is a utility class for producing some statistics on a specific corpus of text.
 * <p>
 * Initialization phase---for a set of sample files of the same length, compare each to each and:
 * <p>
 * <table> 
 * <tr>
 * <td>Compute the mean and standard deviation of the length/C for range of C and N</td>
 * </tr>
 * <tr>
 * <td>Compute the mean and standard deviation of the LD of random texts</td>
 * </tr>
 * <tr>
 * <td>Compute the mean and standard deviation of the LD of signatures of random texts for range of C and N</td>
 * </tr>
 * </table> 
 * <p> 
 * <p>
 * Z-Scoring phase:
 * <p> 
 * <table> 
 * <tr>
 * <td>Suck in the data produced in the one-time initialization phase.</td>
 * </tr>
 * <tr>
 * <td>
 * Execute double Z(int f1Len, int f2Len, String sig1, String sig2), which computes the Z score of  
 * 	the two signatures, i.e., the probability that they could represent unrelated files.
 * </td>
 * </tr>
 * </table> 
 * <p>
 * <p>
 * 
 * This should be 
 * 
 * @author pcoates
 *
 */
public class CentralTendencyCompute {
}
