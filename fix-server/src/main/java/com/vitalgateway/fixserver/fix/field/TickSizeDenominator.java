package com.vitalgateway.fixserver.fix.field;

import quickfix.IntField;

public class TickSizeDenominator extends IntField {

    public static final int FIELD = 5151;

    public TickSizeDenominator() {
        super(5151);
    }

    public TickSizeDenominator(Integer data) {
        super(5151, data);
    }

    public TickSizeDenominator(int data) {
        super(5151, data);
    }
}