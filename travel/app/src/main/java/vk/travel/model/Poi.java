package vk.travel.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Poi implements Parcelable {

    private String id;
    private float lat;
    private float lon;
    private String name;
    private String category;

    public Poi(String id, float lat, float lon, String name, String category) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    // generated code to make objects parcelable ---------------------------------------------------
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeFloat(this.lat);
        dest.writeFloat(this.lon);
        dest.writeString(this.name);
        dest.writeString(this.category);
    }

    protected Poi(Parcel in) {
        this.id = in.readString();
        this.lat = in.readFloat();
        this.lon = in.readFloat();
        this.name = in.readString();
        this.category = in.readString();
    }

    public static final Parcelable.Creator<Poi> CREATOR = new Parcelable.Creator<Poi>() {
        @Override
        public Poi createFromParcel(Parcel source) {
            return new Poi(source);
        }

        @Override
        public Poi[] newArray(int size) {
            return new Poi[size];
        }
    };
}

