package com.vitalgateway.fixserver.fix.message;


import com.vitalgateway.fixserver.fix.field.MarginLevel;
import com.vitalgateway.fixserver.fix.field.MarginUsed;
import com.vitalgateway.fixserver.fix.field.MarginValue;
import quickfix.FieldNotFound;
import quickfix.fix44.Message;

/**
 * @author junyiwan
 * @Description
 * @create 2024-04-17 9:54
 */


public class MarginUpdateMessage extends Message {
    public static final String MSGTYPE = "BI";
    static final long serialVersionUID = 20050617;

    public MarginUpdateMessage() {
        super();
        getHeader().setField(new quickfix.field.MsgType(MSGTYPE));
    }

    public void set(quickfix.field.Account value) {
        setField(value);
    }

    public void set(MarginUsed value) {setField(value);}

    public MarginUsed get(MarginUsed value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public quickfix.field.Account get(quickfix.field.Account value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public quickfix.field.Account getAccount() throws FieldNotFound {
        return get(new quickfix.field.Account());
    }

    public boolean isSet(quickfix.field.Account field) {
        return isSetField(field);
    }


    public void set(MarginValue value) {
        setField(value);
    }

    public MarginValue get(MarginValue value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public MarginValue getMarginValue() throws FieldNotFound {
        return get(new MarginValue());
    }

    public boolean isSet(MarginValue field) {
        return isSetField(field);
    }


    public void set(MarginLevel value) {
        setField(value);
    }

    public MarginLevel get(MarginLevel value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public MarginLevel getMarginLevel() throws FieldNotFound {
        return get(new MarginLevel());
    }

    public boolean isSet(MarginLevel field) {
        return isSetField(field);
    }

    public void set(quickfix.field.MarginExcess value) {
        setField(value);
    }

    public quickfix.field.MarginExcess get(quickfix.field.MarginExcess value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public quickfix.field.MarginExcess getMarginExcess() throws FieldNotFound {
        return get(new quickfix.field.MarginExcess());
    }

    public boolean isSet(quickfix.field.MarginExcess field) {
        return isSetField(field);
    }


    public void set(quickfix.field.Currency value) {
        setField(value);
    }

    public quickfix.field.Currency get(quickfix.field.Currency value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public quickfix.field.Currency getCurrency() throws FieldNotFound {
        return get(new quickfix.field.Currency());
    }

    public boolean isSet(quickfix.field.Currency field) {
        return isSetField(field);
    }
}

