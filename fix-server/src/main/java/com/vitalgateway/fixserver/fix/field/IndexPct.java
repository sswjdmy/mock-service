package com.vitalgateway.fixserver.fix.field;

import quickfix.DoubleField;

public class IndexPct extends DoubleField {
    public static final int FIELD = 6919;

    public IndexPct() {
        super(6919);
    }

    public IndexPct(double data) {
        super(6919, data);
    }
}
