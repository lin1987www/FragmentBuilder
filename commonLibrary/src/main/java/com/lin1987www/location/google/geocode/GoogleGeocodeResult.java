package com.lin1987www.location.google.geocode;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GoogleGeocodeResult implements Parcelable {
    public GoogleGeocodeResult() {
    }

    @JsonCreator
    public GoogleGeocodeResult(
            @JsonProperty("formatted_address") String formatted_address,
            @JsonProperty("types") List<String> types,
            @JsonProperty("geometry") Geometry geometry,
            @JsonProperty("address_components") List<AddressComponent> address_components
    ) {
        this.formatted_address = formatted_address;
        this.types = types;
        this.geometry = geometry;
        this.address_components = address_components;
    }

    public String formatted_address;

    public List<String> types;

    public Geometry geometry;

    public List<AddressComponent> address_components;

    protected String postal_code, country, administrative_area_level_1,
            administrative_area_level_2, administrative_area_level_3, locality,
            sublocality, route, street_number;

    protected void allocation() {
        postal_code = "";
        country = "";
        administrative_area_level_1 = "";
        administrative_area_level_2 = "";
        administrative_area_level_3 = "";
        locality = "";
        sublocality = "";
        route = "";
        street_number = "";
        for (AddressComponent component : address_components) {
            if (component.types.contains("postal_code")) {
                postal_code = component.long_name;
            } else if (component.types.contains("country")) {
                country = component.long_name;
            } else if (component.types.contains("administrative_area_level_1")) {
                administrative_area_level_1 = component.long_name;
            } else if (component.types.contains("administrative_area_level_2")) {
                administrative_area_level_2 = component.long_name;
            } else if (component.types.contains("administrative_area_level_3")) {
                administrative_area_level_3 = component.long_name;
            } else if (component.types.contains("locality")) {
                locality = component.long_name;
            } else if (component.types.contains("sublocality")) {
                sublocality = component.long_name;
            } else if (component.types.contains("route")) {
                route = component.long_name;
            } else if (component.types.contains("street_number")) {
                street_number = component.long_name;
            }
        }
    }

    public String getPostalCode() {
        if (postal_code == null) {
            allocation();
        }
        return postal_code;
    }

    public String getCountry() {
        if (country == null) {
            allocation();
        }
        return country;
    }

    public String getAdministrativeAreaLevel_1() {
        if (administrative_area_level_1 == null) {
            allocation();
        }
        return administrative_area_level_1;
    }

    public String getAdministrativeAreaLevel_2() {
        if (administrative_area_level_2 == null) {
            allocation();
        }
        return administrative_area_level_2;
    }

    public String getAdministrativeAreaLevel_3() {
        if (administrative_area_level_3 == null) {
            allocation();
        }
        return administrative_area_level_3;
    }

    public String getLocality() {
        if (locality == null) {
            allocation();
        }
        return locality;
    }

    public String getSubLocality() {
        if (sublocality == null) {
            allocation();
        }
        return sublocality;
    }

    public String getRoute() {
        if (route == null) {
            allocation();
        }
        return route;
    }

    public String getStreetNumber() {
        if (street_number == null) {
            allocation();
        }
        return street_number;
    }

    /**
     * 用於區分不同區域的字串
     *
     * @return
     */
    public String getArea() {
        return String.format("%1$s%2$s%3$s%4$s",
                getAdministrativeAreaLevel_1(),
                getAdministrativeAreaLevel_2(),
                getAdministrativeAreaLevel_3(), getLocality());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.formatted_address);
        dest.writeList(this.types);
        dest.writeParcelable(this.geometry, 0);
        dest.writeTypedList(address_components);
        dest.writeString(this.postal_code);
        dest.writeString(this.country);
        dest.writeString(this.administrative_area_level_1);
        dest.writeString(this.administrative_area_level_2);
        dest.writeString(this.administrative_area_level_3);
        dest.writeString(this.locality);
        dest.writeString(this.sublocality);
        dest.writeString(this.route);
        dest.writeString(this.street_number);
    }

    private GoogleGeocodeResult(Parcel in) {
        this.formatted_address = in.readString();
        this.types = new ArrayList<String>();
        in.readList(this.types, String.class.getClassLoader());
        this.geometry = in.readParcelable(Geometry.class.getClassLoader());
        in.readTypedList(address_components, AddressComponent.CREATOR);
        this.postal_code = in.readString();
        this.country = in.readString();
        this.administrative_area_level_1 = in.readString();
        this.administrative_area_level_2 = in.readString();
        this.administrative_area_level_3 = in.readString();
        this.locality = in.readString();
        this.sublocality = in.readString();
        this.route = in.readString();
        this.street_number = in.readString();
    }

    public static final Creator<GoogleGeocodeResult> CREATOR = new Creator<GoogleGeocodeResult>() {
        public GoogleGeocodeResult createFromParcel(Parcel source) {
            return new GoogleGeocodeResult(source);
        }

        public GoogleGeocodeResult[] newArray(int size) {
            return new GoogleGeocodeResult[size];
        }
    };
}
