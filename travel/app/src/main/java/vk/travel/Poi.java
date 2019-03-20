package vk.travel;

import org.json.JSONArray;
import org.json.JSONObject;

public class Poi {

    private String place_id;
    private String name;
    private JSONObject geometry;
    private JSONArray types;
    private JSONArray photos;
    private String icon;

    public Poi(String place_id, String name, JSONObject geometry, JSONArray types, JSONArray photos, String icon) {
        this.place_id = place_id;
        this.name = name;
        this.geometry = geometry;
        this.types = types;
        this.photos = photos;
        this.icon = icon;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getGeometry() {
        return geometry;
    }

    public void setGeometry(JSONObject geometry) {
        this.geometry = geometry;
    }

    public JSONArray getTypes() {
        return types;
    }

    public void setTypes(JSONArray types) {
        this.types = types;
    }

    public JSONArray getPhotos() {
        return photos;
    }

    public void setPhotos(JSONArray photos) {
        this.photos = photos;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}

