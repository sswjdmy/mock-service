package com.vitalgateway.fixserver.fix.message;


import com.vitalgateway.fixserver.fix.field.Balance;
import com.vitalgateway.fixserver.fix.field.Credit;
import quickfix.FieldNotFound;
import quickfix.fix44.Message;

/**
 * @author junyiwan
 * @Description
 * @create 2024-04-17 9:54
 */


public class BalanceUpdateMessage extends Message {
    static final long serialVersionUID = 20050617;
    public static final String MSGTYPE = "BJ";

    public BalanceUpdateMessage(){
        super();
        getHeader().setField(new quickfix.field.MsgType(MSGTYPE));
    }

    public void set(quickfix.field.Account value) {
        setField(value);
    }
    public quickfix.field.Account get(quickfix.field.Account value) throws FieldNotFound {
        getField(value);
        return value;
    }

    public void set(Credit value) {
        setField(value);
    }

    public quickfix.field.Account getAccount() throws FieldNotFound {
        return get(new quickfix.field.Account());
    }
    public boolean isSet(quickfix.field.Account field) {
        return isSetField(field);
    }


    public void set(Balance value) {
        setField(value);
    }
    public Balance get(Balance value) throws FieldNotFound {
        getField(value);
        return value;
    }
    public Balance getBalance() throws FieldNotFound {
        return get(new Balance());
    }
    public boolean isSet(Balance field) {
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

