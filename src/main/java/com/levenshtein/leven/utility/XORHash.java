package com.levenshtein.leven.utility;
import org.apache.log4j.Logger;

public class XORHash {
    static Logger log = Logger.getLogger(com.levenshtein.leven.RollingHash.class);

    private int n;
    private char [] chars;

    @SuppressWarnings("unused")
    private XORHash(){
    }



    /**
     * XOR The next N characters in the string into a single value.
     * Can easily modify to XOR character at pos-1 and XOR in the
     * character at pos+n but let's make it work first.
     *
     * XOR is commutative and its own inverse.
     *
     * THe v
     * @param str
     * @param pos
     * @return
     */
    public int hash(String str, int pos){
       int v = 0;
       int stop = Math.min(pos+n, str.length());
       int sLen = str.length();
       for (int i=pos; i<pos+n; i++) {
           if (i>=sLen) {
               break;
           }
           int digit = Character.getNumericValue(str.charAt(i));
           v = v^digit;
       }
       return v;
    }

    /**
     *  Given that the host object is initialized with a characters set and a value
     *  for n, return the pseudo-randomized value for the neighborhood.
     *
     *  Call this for every successive neighborhood in the string to be crunched.
     *
      * @param str The string you are crunching.
     * @param pos Then current neighborhood in the string.
     * @return
     */
    public char map(String str, int pos){
        return chars[hash(str,pos) % chars.length];
    }


    /**
     * Create a hash object for a given set of parameters.
     * If you n on the same substring you should get the same result every time.
     *
     * @param n int Neighborhood size, e.g., 20
     * @param chars Character[] The alphabet of output characters
     */
    public XORHash(int n, char[] chars) {
        this.n = n;
        this.chars = chars;
    }
}
