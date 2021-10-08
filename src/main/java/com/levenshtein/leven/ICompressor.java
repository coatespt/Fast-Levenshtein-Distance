package com.levenshtein.leven;

/**
 * Contract for a string compressor. Implementors produce the signatures that are compared by
 * LD to derive an estimate. Typical compression rate 25X to 300X depending on application.
 * <p>
 * @author pcoates
 */
public abstract class ICompressor {
    protected static int prime = 39595541;
    private Integer n;
    private Integer c;

    // Default to squeezing out the white space.
    protected static boolean SQUEEZE_WHITE = true;
    public static boolean PRINT_DIAGNOSTICS = false;

    // This defaults to 83 characters. because that the largest prime that includes most of the
    // ASCII set that is printable but are unlikely to affect CSV formatting.
    // You can go larger if you want.  The length of this string should be mutually prime wrt C.
    // TODO: I think this could contain arbitrary Unicode but it's not tested.
    private static String chars = "abcdefghijklmnopqrstuvwxsyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456!@#$%^&*()_-+=[]{};:<>.?";
    public static String getChars() {
        return chars;
    }

    public static void setChars(String ch) {
        chars = ch;
    }

    /**
     * Turns off/on this behavior. This can give more realistic results for
     * formatted input such as computer code.
     *
     * @param b
     */
    public static void setSqueezeWhite(boolean b) {
        SQUEEZE_WHITE = b;
    }

    /**
     * Replace all multiple white spaces in the string with a single white space.
     * @param value
     * @return
     */
    public String squeezeWhite(String value) {
        String str = value.replaceAll("\\s+", " ");
        return str.trim();
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    // Any implementor has to create this method.
    public abstract String _compress(String str) throws Exception;

    /**
     * Compress input string into a hashed signature;
     */
    public String compress(String str) {
        try {
            if (SQUEEZE_WHITE) {
                str = squeezeWhite(str);
            }
            return _compress(str);
        } catch (Exception x) {
            System.err.println("compress() failed in ICompressor abstract class.");
            return null;
        }
    }

    // Convert a string to an array of characters.
    //
    public static char[] StringToCharArray(String ch) throws Exception {
        String chars = ch;
        if (chars == null || chars.length() < 26) {
            // Sanity check--so few chars means you probably passed in the wrong string. This can be changed or removed.
            throw new Exception("output characters set null or very short in StringToCharArray method");
        }
        char[] tmp = new char[chars.length()];
        for (int i = 0; i < chars.length(); i++) {
            tmp[i] = chars.charAt(i);
        }
        return tmp;
    }
}
