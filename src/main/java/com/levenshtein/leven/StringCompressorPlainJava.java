package com.levenshtein.leven;

public class StringCompressorPlainJava extends ICompressor{


    // TODO MOVE THIS UP
    char[] outputChars = null;
    public StringCompressorPlainJava(int n, int c, char[] outputChars){
        setC(c);
        setN(n);
        this.outputChars=outputChars;
    }

    /**
     * Implementation of compression into hashed signature.
     * @param str String Any UTF-8 string.
     * @return String a hashed signature.
     * @throws Exception
     */
    public String _compress(String str) throws Exception {
        StringBuffer sb = new StringBuffer();
        int len=str.length();
        int maxPos = len-getN();
        if(maxPos<=0){
            return "";
        }
        for(int i=0; i<maxPos; i++){
            String neighborhood = str.substring(i, i+getN());
            int hashval = Math.abs(neighborhood.hashCode());
            int noRet = hashval % getC();
            if(noRet != 0){
               continue;
            }
            int l = outputChars.length;
            int ind= hashval % l;
            Character chOut = outputChars[ind];
            sb.append(chOut);
        }
        if(PRINT_DIAGNOSTICS){
            System.out.println(sb);
        }
        return sb.toString();
    }
}
