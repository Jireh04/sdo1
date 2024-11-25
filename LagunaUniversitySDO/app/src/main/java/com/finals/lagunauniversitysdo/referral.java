package com.finals.lagunauniversitysdo;

public class referral {
    private String referrer;
    private String date;
    private String action;  // For setting the action label (View, Accept, Reject)

    public referral(String referrer, String date, String action) {
        this.referrer = referrer;
        this.date = date;
        this.action = action;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }
}
