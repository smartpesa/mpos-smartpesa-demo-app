package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class UILoyalty implements Parcelable {

    String trackingId;
    ArrayList<UIRedeemable> redemptionList;

    public UILoyalty(String trackingId, ArrayList<UIRedeemable> redemptionList) {
        this.trackingId = trackingId;
        this.redemptionList = redemptionList;
    }

    public UILoyalty(Parcel in) {
        trackingId = in.readString();
        redemptionList = in.readArrayList(UIRedeemable.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackingId);
        dest.writeList(redemptionList);
    }

    public static final Parcelable.Creator<UILoyalty> CREATOR = new Parcelable.Creator<UILoyalty>() {
        public UILoyalty createFromParcel(Parcel source) {
            return new UILoyalty(source);
        }

        @Override
        public UILoyalty[] newArray(int size) {
            return new UILoyalty[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public List<UIRedeemable> getRedemptionList() {
        return redemptionList;
    }

    public void setRedemptionList(ArrayList<UIRedeemable> redemptionList) {
        this.redemptionList = redemptionList;
    }
}
