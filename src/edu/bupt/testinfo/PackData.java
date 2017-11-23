package edu.bupt.testinfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PackData {

    public JSONObject packDownloadData(String time, int ave_download_speed, int max_download_speed, double gps_lat,
            double gps_lon, String server_url, String networkType, String imei, String internal_ip, String external_ip) {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("test_time_index", "" + time);
            jsonObject.put("ave_downloadSpeed", "" + ave_download_speed);
            jsonObject.put("max_downloadSpeed", "" + max_download_speed);
            jsonObject.put("gps_lat", "" + gps_lat);
            jsonObject.put("gps_lon", "" + gps_lon);

            jsonObject.put("server_url", "" + server_url);
            jsonObject.put("network_type", networkType);
            jsonObject.put("imei", "" + imei);

            jsonObject.put("internal_IP", "" + internal_ip);
            jsonObject.put("external_IP", "" + external_ip);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject packUploadData(String time, int ave_upload_speed, int max_upload_speed, double gps_lat,
            double gps_lon, String server_url, String networkType, String imei, String internal_ip, String external_ip) {

        JSONObject jsonObject = new JSONObject();
        try {
            Log.w("packUploadData", "packUploadData.toString() 1");

            jsonObject.put("test_time_index", "" + time);
            jsonObject.put("ave_uploadSpeed", "" + ave_upload_speed);
            jsonObject.put("max_uploadSpeed", "" + max_upload_speed);
            jsonObject.put("gps_lat", "" + gps_lat);
            jsonObject.put("gps_lon", "" + gps_lon);

            jsonObject.put("server_url", "" + server_url);
            jsonObject.put("network_type", networkType);
            jsonObject.put("imei", "" + imei);
            jsonObject.put("internal_IP", "" + internal_ip);
            jsonObject.put("external_IP", "" + external_ip);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject packLatencyData(String time, int latency, double gps_lat, double gps_lon, String server_url,
            String networkType, String imei, String internal_ip, String external_ip) {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("test_time_index", "" + time);
            jsonObject.put("latency", "" + latency);
            jsonObject.put("gps_lat", "" + gps_lat);
            jsonObject.put("gps_lon", "" + gps_lon);

            jsonObject.put("server_url", "" + server_url);
            jsonObject.put("network_type", networkType);
            jsonObject.put("imei", "" + imei);

            jsonObject.put("internal_IP", "" + internal_ip);
            jsonObject.put("external_IP", "" + external_ip);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject packThirdDownloadData(String time, int connection_time, int first_byte_time, int download_time,
            double gps_lat, double gps_lon, String server_url, String networkType, String imei, String internal_ip,
            String external_ip) {

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("test_time_index", "" + time);
            jsonObject.put("gps_lat", "" + gps_lat);
            jsonObject.put("gps_lon", "" + gps_lon);

            jsonObject.put("connection_time", "" + connection_time);
            jsonObject.put("first_byte_time", "" + first_byte_time);
            jsonObject.put("download_time", "" + download_time);

            jsonObject.put("server_url", "" + server_url);
            jsonObject.put("network_type", networkType);
            jsonObject.put("imei", "" + imei);

            jsonObject.put("internal_IP", "" + internal_ip);
            jsonObject.put("external_IP", "" + external_ip);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
