package com.example.nearmedemo.Model;

public class ProductModel {

    private String name, price, location, url, pushKey, shopName;

    public ProductModel() {
    }

    public ProductModel(String name, String price, String location, String url, String pushKey, String shopName) {
        this.name = name;
        this.price = price;
        this.location = location;
        this.url = url;
        this.pushKey = pushKey;
        this.shopName = shopName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getPushKey() {
        return pushKey;
    }

    public void setPushKey(String pushKey) {
        this.pushKey = pushKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
