package com.softinc.bean;

import com.softinc.zxing.decoding.Intents;

public class Coin {
    private Integer ID;
    private String Name;
    private String StoreCode;
    private String Val;
    private Integer State;
    private String Type;
    private Integer Price;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStoreCode() {
        return StoreCode;
    }

    public void setStoreCode(String storeCode) {
        StoreCode = storeCode;
    }

    public String getVal() {
        return Val;
    }

    public void setVal(String val) {
        Val = val;
    }

    public Integer getState() {
        return State;
    }

    public void setState(Integer state) {
        State = state;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public Integer getPrice() {
        return Price;
    }

    public void setPrice(Integer price) {
        Price = price;
    }
}
