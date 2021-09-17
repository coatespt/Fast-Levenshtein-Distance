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

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
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

    // TODO: You can discard the development test. It's just here to see it work.
    public static void main(String args[]) {
        try {
            // read in a realistic size file.
            String filename = "./data/10001.txt";
            String longString = FileAndTimeUtility.getFileContents(filename);

            // a million iterations so it takes long enough for a reliable elapsed time.
            int ITER = 1000000;

            // We want to take a different substring for each neighborhood calculation
            // This turns out to only a minor speed decrease of using a hard-coded string.
            int start = 3210;
            int rollover = 5000;
            int newPos = 0;

            // the neighborhood size, i.e., the length of the string we are hashing
            int n = 17;

            // So we can print out the last one.
            String input = "";
            // The ret string so we can print out the last one
            String ret = "";

            long hash64 = 0;
            long startTime = (new Date()).getTime();
            for (int i = 0; i < ITER; i++) {
                int p1 = start + newPos;
                int p2 = p1 + n;
                if (newPos++ > rollover) {
                    newPos = 0;
                }
                input = longString.substring(p1, p2);
                // Note, just the SHA256 runs at 1.706 million/second
                byte[] bits64 = getSHA(input);
                // plus converting 32 bytes to a long adds just a few percent total 1.858 seconds.
                hash64 = bytesToLong256(bits64);

                // With conversion to string rep of hex value is expensive! 550K/second, i.e. more than double
                //   the cost of the SHA256 hash. We wouldn't use it anyway, but it's interesting.
                // ret = toHexString(getSHA(input));
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

    @Override
    public String compress(String str) {
        return null;
    }
}
