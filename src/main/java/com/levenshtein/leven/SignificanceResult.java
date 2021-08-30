package com.levenshtein.leven;

public class SignificanceResult {
    private int sig1Len;
    private int sig2Len;
    private int rawSigLD;
    private double expectedSigLD;
    private double computedSignificane;
    private Boolean isSignificnt=false;
    private double x;

    public SignificanceResult(int sig1length, int sig2Length, int rawSigLD, double expectedSigLD, double computedSignificane, Boolean isSignificnt, double x) {
        this.sig1Len=sig1length;
        this.sig2Len=sig2Length;
        this.rawSigLD = rawSigLD;
        this.expectedSigLD = expectedSigLD;
        this.computedSignificane = computedSignificane;
        this.isSignificnt = isSignificnt;
        this.x = x;
    }

    public String toString(){
       StringBuffer sb = new StringBuffer(100);
       sb.append("Significant at ");
        sb.append(x);
        sb.append(" sigLd: ");
        sb.append(rawSigLD);
        sb.append(" expectedLD: ");
        sb.append(expectedSigLD);
        sb.append(" significane:");
        sb.append(computedSignificane);
        sb.append(" sig1Len: ");
        sb.append(sig1Len);
        sb.append(" sig2Len: ");
        sb.append(sig2Len);
        return sb.toString();
    }

    public int getSig1Len(){
        return sig1Len;
    }

    public int getSig2Len(){
        return sig2Len;
    }

    public void setSig1Len(int n){
        sig1Len=n;
    }

    public void setSig2Len(int n){
        sig2Len=n;
    }

    public int getSigLenDiff(){
        return Math.abs(sig1Len-sig2Len);
    }

    public int getRawSigLD() {
        return rawSigLD;
    }

    public void setRawSigLD(int rawSigLD) {
        this.rawSigLD = rawSigLD;
    }

    public double getExpectedSigLD() {
        return expectedSigLD;
    }

    public void setExpectedSigLD(double expectedSigLD) {
        this.expectedSigLD = expectedSigLD;
    }

    public double getComputedSignificane() {
        return computedSignificane;
    }

    public void setComputedSignificane(double computedSignificane) {
        this.computedSignificane = computedSignificane;
    }

    public Boolean getSignificnt() {
        return isSignificnt;
    }

    public void setSignificnt(Boolean significnt) {
        isSignificnt = significnt;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }


}
