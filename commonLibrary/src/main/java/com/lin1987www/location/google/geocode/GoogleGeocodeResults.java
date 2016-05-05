package com.lin1987www.location.google.geocode;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Locale;

public class GoogleGeocodeResults implements Parcelable {
    public String status;
    public List<GoogleGeocodeResult> results;

    @JsonCreator
    public GoogleGeocodeResults(
            @JsonProperty("status")String status,
            @JsonProperty("results")List<GoogleGeocodeResult> results
    ) {
        this.status = status;
        this.results =results;
    }

    protected String postal_code, country, administrative_area_level_1,
            administrative_area_level_2, administrative_area_level_3, locality,
            sublocality, route, street_number;

    protected String mResponseText;

    public String getResponseText() {
        return mResponseText;
    }

    public void setResponseText(String pResponseText) {
        mResponseText = pResponseText;
    }

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
        for (GoogleGeocodeResult result : results) {
            if (TextUtils.isEmpty(postal_code)) {
                postal_code = result.getPostalCode();
            }
            if (TextUtils.isEmpty(country)) {
                country = result.getCountry();
            }
            if (TextUtils.isEmpty(administrative_area_level_1)) {
                administrative_area_level_1 = result
                        .getAdministrativeAreaLevel_1();
            }
            if (TextUtils.isEmpty(administrative_area_level_2)) {
                administrative_area_level_2 = result
                        .getAdministrativeAreaLevel_2();
            }
            if (TextUtils.isEmpty(administrative_area_level_3)) {
                administrative_area_level_3 = result
                        .getAdministrativeAreaLevel_3();
            }
            if (TextUtils.isEmpty(locality)) {
                locality = result.getLocality();
            }
            if (TextUtils.isEmpty(sublocality)) {
                sublocality = result.getSubLocality();
            }
            if (TextUtils.isEmpty(route)) {
                route = result.getRoute();
            }
            if (TextUtils.isEmpty(street_number)) {
                street_number = result.getRoute();
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

    public String getArea() {
        String area = String.format("%1$s%2$s%3$s%4$s",
                getAdministrativeAreaLevel_1(),
                getAdministrativeAreaLevel_2(),
                getAdministrativeAreaLevel_3(), getLocality());
        if (TextUtils.isEmpty(area)) {
            area = getCountry();
        }
        return area;
    }

    public Address getAddress(Locale pLocale, double pLatitude,
                              double pLongitude) {
        Address address = new Address(pLocale);
        address.setPostalCode(getPostalCode());
        address.setCountryName(getCountry());
        address.setAdminArea(getAdministrativeAreaLevel_1());
        address.setSubAdminArea(getAdministrativeAreaLevel_2());
        address.setLocality(getLocality());
        address.setSubLocality(getSubLocality());
        address.setLatitude(pLatitude);
        address.setLongitude(pLongitude);
        return address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.status);
        dest.writeTypedList(results);
        dest.writeString(this.postal_code);
        dest.writeString(this.country);
        dest.writeString(this.administrative_area_level_1);
        dest.writeString(this.administrative_area_level_2);
        dest.writeString(this.administrative_area_level_3);
        dest.writeString(this.locality);
        dest.writeString(this.sublocality);
        dest.writeString(this.route);
        dest.writeString(this.street_number);
        dest.writeString(this.mResponseText);
    }

    private GoogleGeocodeResults(Parcel in) {
        this.status = in.readString();
        in.readTypedList(results, GoogleGeocodeResult.CREATOR);
        this.postal_code = in.readString();
        this.country = in.readString();
        this.administrative_area_level_1 = in.readString();
        this.administrative_area_level_2 = in.readString();
        this.administrative_area_level_3 = in.readString();
        this.locality = in.readString();
        this.sublocality = in.readString();
        this.route = in.readString();
        this.street_number = in.readString();
        this.mResponseText = in.readString();
    }

    public static final Creator<GoogleGeocodeResults> CREATOR = new Creator<GoogleGeocodeResults>() {
        public GoogleGeocodeResults createFromParcel(Parcel source) {
            return new GoogleGeocodeResults(source);
        }

        public GoogleGeocodeResults[] newArray(int size) {
            return new GoogleGeocodeResults[size];
        }
    };
}
