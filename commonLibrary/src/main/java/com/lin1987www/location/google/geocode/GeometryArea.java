package com.lin1987www.location.google.geocode;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeometryArea implements Parcelable {
    public GeometryLatLng northeast;
    public GeometryLatLng southwest;

    @JsonCreator
    public GeometryArea(
            @JsonProperty("northeast") GeometryLatLng northeast,
            @JsonProperty("southwest") GeometryLatLng southwest
    ) {
        this.northeast = northeast;
        this.southwest = southwest;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.northeast, 0);
        dest.writeParcelable(this.southwest, 0);
    }

    private GeometryArea(Parcel in) {
        this.northeast = in.readParcelable(GeometryLatLng.class.getClassLoader());
        this.southwest = in.readParcelable(GeometryLatLng.class.getClassLoader());
    }

    public static final Creator<GeometryArea> CREATOR = new Creator<GeometryArea>() {
        public GeometryArea createFromParcel(Parcel source) {
            return new GeometryArea(source);
        }

        public GeometryArea[] newArray(int size) {
            return new GeometryArea[size];
        }
    };
}
