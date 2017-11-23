package com.docomo.serverSelection;

/**
 * 
 * 用于存储server的信息，包括 server :id,url，name，lat，lon,city，sponsor和level；
 * level表示健康状态，默认为5，测试失败一次，减1，数据很好的情况下加1；数值越高越好
 * 
 * */
public class ServerData {
    private int id;// server id
    private String url = "";// server url
    private String name = "";// server 名称
    private String city = "";// server 所在城市
    private double lat;// server 所在纬度
    private double lon;// server 所在经度
    private String Sponsor = "";// /server赞助商
    private int level;// server 健康水平

    public ServerData(int id, String url, String name, String city, double lat, double lon, String Sponsor, int level) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
        this.Sponsor = Sponsor;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServerName() {
        return name;
    }

    public void setServerName(String name) {
        this.name = name;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getSponsor() {
        return this.Sponsor;
    }

    public void setSponsor(String sp) {
        this.Sponsor = sp;
    }

    public int getLevel() {
        return this.level;

    }

    public void setLevel(int level) {
        this.level = level;

    }

}
