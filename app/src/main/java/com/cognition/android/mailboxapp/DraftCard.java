package com.cognition.android.mailboxapp;
import android.widget.Button;

public class DraftCard {
    private String line1;
    private String label;
    private String line2;
    private Button billbtn;

    public DraftCard(String line1, String  label, String line2, Button billbtn) {
        this.line1 = line1;
        this.label = label;
        this.line2 = line2;
        this.billbtn = billbtn;
    }

    public DraftCard(String line1, String line2) {
        this.line1 = line1;
        this.line2 = line2;
    }
    public String getLine1() {
        return line1;
    }

    public String getLabel() { return label;}

    public String getLine2() {
        return line2;
    }

    public Button getBillbtn() { return billbtn; }
}