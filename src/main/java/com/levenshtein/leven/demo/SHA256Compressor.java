package com.levenshtein.leven.demo;

import com.levenshtein.leven.ICompressor;
import utilities.file.FileAndTimeUtility;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class SHA256Compressor extends ICompressor {

    private char[] outputChars = null;
    public SHA256Compressor(int n, int c, char[] outputChars){
        setC(c);
        setN(n);
        this.outputChars=outputChars;
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public String _compress(String str) throws Exception {
        StringBuffer sb = new StringBuffer();
        int len=str.length();
        int maxPos = len-getN();
        if(maxPos<=0){
            return "";
        }
        for(int i=0; i<maxPos; i++){
            String neighborhood = str.substring(i,i+getN());
            byte[] bytes = getSHA(neighborhood);
            long hl = bytesToLong256(bytes);
            // coercion to int simply takes the low-order 32 bits which I think is negative if the 32nd bit is set.
            int hashVal = Math.abs((int) hl * prime);
            if(hashVal % getC() != 0){
                continue;
            }
            Character chOut = outputChars[hashVal % outputChars.length];
            sb.append(chOut);
        }
        if(PRINT_DIAGNOSTICS){
            System.out.println(sb);
        }
        return sb.toString();
    }

    // TODO: You can discard the development test. It's just here to see it work.
    public static void main(String args[]) {
        try {
            // read in a realistic size file and run SHA on it ITER times.
            String filename = "./data/10001.txt";

            // A lot of iterations so it takes long enough for a reliable elapsed time.
            int ITER = 100000000;

            // the neighborhood size, i.e., the length of the string we are hashing
            int n = 17;

            // So we can print out the last one.
            String input = "";
            // The ret string so we can print out the last one
            String ret = "";

            long hash64 = 0;

            long startTime = (new Date()).getTime();
            int computations=0;
            while (true) {
                String longString = FileAndTimeUtility.getFileContents(filename);
                longString = longString.replaceAll("\\s+", " ");

                int len = longString.length() - n;
                for (int i = 0; i < len; i++) {
                    input = longString.substring(i, i + n);
                    // Note, just the SHA256 runs at 1.706 million/second
                    byte[] bits64 = getSHA(input);
                    // plus converting 32 bytes to a long adds just a few percent total 1.858 seconds.
                    hash64 = Math.abs(bytesToLong256(bits64));
                    // With conversion to string rep of hex value is expensive! 550K/second, i.e. more than double
                    //   the cost of the SHA256 hash. We wouldn't use it anyway, but it's interesting.
                    // ret = toHexString(getSHA(input));
                    if (computations++ > ITER) {
                        break;
                    }
                }
                if (computations>ITER){
                    break;
                }
            }
            long endTime = (new Date()).getTime();
            long elapsed = endTime - startTime;
            double ratePerSecond = ITER / (double) elapsed * 1000d;
            System.out.println("\n[" + input + "]  hashed to: " + ret + " long:" + hash64 + " in:" + elapsed + " milliseconds = " + ratePerSecond + "/sec");
        }
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown for incorrect algorithm: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static long bytesToLong256(byte[] bytehash) {
        int len = bytehash.length;
        int chunk = 8;
        long accumulator = 0;
        for (int i = 0; i < 4; i++) {
            int start = i * chunk;
            byte[] bytes = new byte[chunk];
            for (int j = 0; j < chunk; j++) {
                bytes[j] = bytehash[start + j];
            }
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            long section = bb.getLong();
            accumulator = accumulator ^ section;
        }
        return  accumulator;
    }

}
