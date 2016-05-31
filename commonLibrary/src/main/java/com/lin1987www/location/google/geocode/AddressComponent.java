package com.lin1987www.location.google.geocode;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class AddressComponent implements Parcelable {
    public String long_name;
    public String short_name;
    public ArrayList<String> types;

    @JsonCreator
    public AddressComponent(
            @JsonProperty("long_name") String long_name,
            @JsonProperty("short_name") String short_name,
            @JsonProperty("types") ArrayList<String> types) {
        this.long_name = long_name;
        this.short_name = short_name;
        this.types = types;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.long_name);
        dest.writeString(this.short_name);
        dest.writeSerializable(this.types);
    }

    private AddressComponent(Parcel in) {
        this.long_name = in.readString();
        this.short_name = in.readString();
        this.types = (ArrayList<String>) in.readSerializable();
    }

    public static final Creator<AddressComponent> CREATOR = new Creator<AddressComponent>() {
        public AddressComponent createFromParcel(Parcel source) {
            return new AddressComponent(source);
        }

        public AddressComponent[] newArray(int size) {
            return new AddressComponent[size];
        }
    };
}
