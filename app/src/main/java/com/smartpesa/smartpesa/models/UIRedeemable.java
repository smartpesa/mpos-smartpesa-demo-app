package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class UIRedeemable implements Parcelable {

    String id;
    String name;
    int type;
    BigDecimal amount;
    String unit;
    BigDecimal balance;
    boolean isSelected;

    public UIRedeemable(String id, String name, int type, BigDecimal amount, String unit, BigDecimal balance, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.unit = unit;
        this.balance = balance;
        this.isSelected = isSelected;
    }

    public UIRedeemable(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readInt();
        amount = new BigDecimal(in.readString());
        unit = in.readString();
        balance = new BigDecimal(in.readString());
        isSelected  = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeString(amount.toString());
        dest.writeString(unit);
        dest.writeString(balance.toString());
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    public static final Parcelable.Creator<UIRedeemable> CREATOR = new Parcelable.Creator<UIRedeemable>() {
        public UIRedeemable createFromParcel(Parcel source) {
            return new UIRedeemable(source);
        }

        @Override
        public UIRedeemable[] newArray(int size) {
            return new UIRedeemable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
