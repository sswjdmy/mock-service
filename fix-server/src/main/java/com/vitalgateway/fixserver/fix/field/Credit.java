package com.vitalgateway.fixserver.fix.field;

import quickfix.DoubleField;


public class Credit extends DoubleField {

    public static final int FIELD = 20005;


    public Credit() {
        super(20005);
    }

    public Credit(Double data) {
        super(20005, data);
    }
}