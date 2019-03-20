package vk.travel;

import org.json.JSONArray;
import org.json.JSONObject;

public class Poi {

    private String id;
    private String name;
    private JSONObject geometry;
    private JSONArray types;
    private JSONArray titlePhotos;
    private String icon;
    private String address;
    private JSONArray photos;
    private String phone;
    private String url;
    private String web;
    private Double rating;
    private JSONArray schedule;
    private JSONArray reviews;
    private int index;

    public Poi(String id, String name, JSONObject geometry, JSONArray types, JSONArray titlePhotos, String icon) {
        this.id = id;
        this.name = name;
        this.geometry = geometry;
        this.types = types;
        this.titlePhotos = titlePhotos;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public JSONArray getTitlePhotos() {
        return titlePhotos;
    }
    public void setTitlePhotos(JSONArray titlePhotos) {
        this.titlePhotos = titlePhotos;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public JSONArray getPhotos() {
        return photos;
    }
    public void setPhotos(JSONArray photos) {
        this.photos = photos;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getWeb() {
        return web;
    }
    public void setWeb(String web) {
        this.web = web;
    }
    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public JSONArray getSchedule() {
        return schedule;
    }
    public void setSchedule(JSONArray schedule) {
        this.schedule = schedule;
    }
    public JSONArray getReviews() {
        return reviews;
    }
    public void setReviews(JSONArray reviews) {
        this.reviews = reviews;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}

