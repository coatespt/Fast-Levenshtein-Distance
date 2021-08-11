package com.levenshtein.leven.utility;
import org.apache.log4j.Logger;

public class XORHash {
    static Logger log = Logger.getLogger(com.levenshtein.leven.RollingHash.class);

    private int n;
    private int c;
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
     * I tried this with XOR'ing the characters all into one. This doesn't work because the
     * cardinality of the values is too small to allow you to take 1/c of them. Doh. You end
     * up with just a few output characters repeated many time.
     *
     * Solution is XOR them into an initially blank long treated as a circular buffer.
     *
     * For neigborhoods up to 8, it's the same as treating the neighborhood as a
     * bit field.
     *
     * For neighborhoods that are longer than eight, each additional character combines its
     * value with an earlier value.
     *
     * @param str
     * @param pos
     * @return
     */
    public int hash(String str, int pos){
       int v = 0;
       int stop = Math.min(pos+n, str.length());
       int sLen = str.length();
       long accum=0;
       int shiftpos = 0;
       for (int i=pos; i<pos+n; i++) {
           if (i>=sLen) {
               break;
           }
           int currentChar = str.charAt(i);
           int shiftDistance = 8 * (shiftpos++ % 8);
           currentChar = currentChar << shiftDistance;
           v = v^currentChar;
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
    public int map(String str, int pos){
        int h = hash(str,pos);
        int mn = Math.min(pos+n, str.length());
        String substr = str.substring(pos,mn);
        int hc = substr.hashCode();
        if (hc%c == 7){
            return new Character(chars[h % chars.length]);
        } else {
            return -1;
        }
    }


    /**
     * Create a hash object for a given set of parameters.
     * If you n on the same substring you should get the same result every time.
     *
     * @param n int Neighborhood size, e.g., 20
     * @param chars Character[] The alphabet of output characters
     */
    public XORHash(int n, int c,  char[] chars) {
        this.n = n;
        this.c = c;
        this.chars = chars;
    }
}
