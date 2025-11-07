package com.vitalgateway.fixserver.fix.field;

import quickfix.DoubleField;

public class MarginUsed  extends DoubleField {
    public static final int FIELD = 20004;

    public MarginUsed() {
        super(FIELD);
    }

    public MarginUsed(double data) {
        super(FIELD, data);
    }

}
