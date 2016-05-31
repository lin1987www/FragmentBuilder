package com.lin1987www.location.google.geocode;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeometryLatLng implements Parcelable {
    public Float lat;
    public Float lng;

    @JsonCreator
    public GeometryLatLng(
            @JsonProperty("lat") Float lat,
            @JsonProperty("lng") Float lng
    ) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.lat);
        dest.writeValue(this.lng);
    }

    private GeometryLatLng(Parcel in) {
        this.lat = (Float) in.readValue(Float.class.getClassLoader());
        this.lng = (Float) in.readValue(Float.class.getClassLoader());
    }

    public static final Creator<GeometryLatLng> CREATOR = new Creator<GeometryLatLng>() {
        public GeometryLatLng createFromParcel(Parcel source) {
            return new GeometryLatLng(source);
        }

        public GeometryLatLng[] newArray(int size) {
            return new GeometryLatLng[size];
        }
    };
}
