package vk.travel.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Poi implements Parcelable {

    private String id;
    private float lat;
    private float lon;
    private String name;
    private String tourism;

    public Poi() {}

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

    public String getTourism() {
        return tourism;
    }

    public void setTourism(String tourism) {
        this.tourism = tourism;
    }

    // parcelable plugin ---------------------------------------------------------------------------
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
        dest.writeString(this.tourism);
    }

    protected Poi(Parcel in) {
        this.id = in.readString();
        this.lat = in.readFloat();
        this.lon = in.readFloat();
        this.name = in.readString();
        this.tourism = in.readString();
    }

    public static final Creator<Poi> CREATOR = new Creator<Poi>() {
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

