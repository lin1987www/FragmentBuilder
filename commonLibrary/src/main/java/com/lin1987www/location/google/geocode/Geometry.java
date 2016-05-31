package com.lin1987www.location.google.geocode;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Geometry implements Parcelable {
    public String location_type;
    public GeometryLatLng location;
    public GeometryArea viewport;
    public GeometryArea bounds;

    @JsonCreator
    public Geometry(
            @JsonProperty("location_type") String location_type,
            @JsonProperty("location") GeometryLatLng location,
            @JsonProperty("viewport") GeometryArea viewport,
            @JsonProperty("bounds") GeometryArea bounds
    ) {
        this.location_type = location_type;
        this.location = location;
        this.viewport = viewport;
        this.bounds = bounds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.location_type);
        dest.writeParcelable(this.location, flags);
        dest.writeParcelable(this.viewport, flags);
        dest.writeParcelable(this.bounds, flags);
    }

    private Geometry(Parcel in) {
        this.location_type = in.readString();
        this.location = in.readParcelable(GeometryLatLng.class.getClassLoader());
        this.viewport = in.readParcelable(GeometryArea.class.getClassLoader());
        this.bounds = in.readParcelable(GeometryArea.class.getClassLoader());
    }

    public static final Creator<Geometry> CREATOR = new Creator<Geometry>() {
        public Geometry createFromParcel(Parcel source) {
            return new Geometry(source);
        }

        public Geometry[] newArray(int size) {
            return new Geometry[size];
        }
    };
}
