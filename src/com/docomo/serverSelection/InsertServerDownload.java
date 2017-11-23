package com.docomo.serverSelection;

import org.json.JSONException;
import org.json.JSONObject;

//import edu.bupt.testinfo.UploadData;

import android.util.Log;

public class InsertServerDownload extends Thread {
    String uploadDataURL = "http://xugang.host033.youdnser.com/dataServer/server_selection/insert_server_sel_download.php";
    String network_type;
    String network_operator;
    String network_standard;
    String server_url;
    int download_speed;

    double lat;
    double lon;

    public InsertServerDownload() {

    }

    public InsertServerDownload(String network_type, String network_operator, String network_standard,
            String server_url, int download_speed, double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.network_type = network_type;
        this.network_operator = network_operator;
        this.network_standard = network_standard;
        this.server_url = server_url;
        this.download_speed = download_speed;
    }

    public void setlat(double lat) {
        this.lat = lat;
    }

    public void setlon(double lon) {
        this.lon = lon;
    }

    public void setnetwork_type(String network_type) {
        this.network_type = network_type;
    }

    public void setnetwork_operator(String network_operator) {
        this.network_operator = network_operator;
    }

    public void setnetwork_standard(String network_standard) {
        this.network_standard = network_standard;
    }

    public void server_url(String server_url) {
        this.server_url = server_url;
    }

    public void server_url(int download_speed) {
        this.download_speed = download_speed;
    }

    public void run() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lat", lat);
            jsonObject.put("lon", lon);
            jsonObject.put("network_type", network_type);
            jsonObject.put("network_operator", network_operator);
            jsonObject.put("network_standard", network_standard);
            jsonObject.put("server_url", server_url);
            jsonObject.put("download_speed", "" + download_speed);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        UploadData u = new UploadData(uploadDataURL, jsonObject);
//        String re = u.upData();
//        Log.v("MainActivity", "insert_download_re:" + re);
    }
}
