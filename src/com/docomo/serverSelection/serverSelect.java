package com.docomo.serverSelection;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import edu.bupt.testinfo.UploadData;

import android.util.Log;

public class serverSelect extends Thread {
    final String uploadDataURL = "http://xugang.host033.youdnser.com/dataServer/server_selection/server_sel.php";

    double lat;
    double lon;
    String network_type;
    String network_operator;
    String network_standard;

    String firstURL = "http://buptant.cn/UNOTest/speedtest";
    String secondURL = "http://speed.dtgt.org/speedtest";
    String thirdURL = "http://xugang.host033.youdnser.com/UNOTest/speedtest";

    List<String> serverListResult;

    public boolean isEnd = false;

    public serverSelect() {
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

    public void run() {
//        isEnd = false;
//        JSONObject jsonObject = new JSONObject();
        try {
//            jsonObject.put("lat", lat);
//            jsonObject.put("lon", lon);
//            jsonObject.put("network_type", network_type);
//            jsonObject.put("network_operator", network_operator);
//            jsonObject.put("network_standard", network_standard);
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
////        UploadData u = new UploadData(uploadDataURL, jsonObject);
////        String re = u.upData();
////        Log.v("MainActivity", "re:" + re);
//        JSONArray ja = null;
//        try {
////            ja = new JSONArray(re);
//            String[] url = new String[ja.length()];
//            int[] downloadAverage = new int[ja.length()];
//            for (int i = 0; i < ja.length(); i++) {
//                url[i] = ja.getJSONObject(i).getString("url");
//                downloadAverage[i] = ja.getJSONObject(i).getInt("downloadAverage");
//            }
//
//            PingServer ps = new PingServer(url, network_type, network_operator, network_standard, lat, lon);
//            ps.run();
//            int[] ping = ps.getPing();
//            float firstMark = 0;
//            float secondMark = 0;
//            float thirdMark = 0;
//            int firstIndex = -1;
//            int secondIndex = -1;
//            int thirdIndex = -1;
//            for (int i = 0; i < ja.length(); i++) {
//                if (downloadAverage[i] <= 0) {
//                    downloadAverage[i] = 100;
//                }
//                float mark = (float) ((ping[i] > 1000 ? 0 : 1000 - ping[i]) * 0.2 + (downloadAverage[i] > 1000 ? 1000
//                        : downloadAverage[i]) * 0.8);
//                if (mark > firstMark) {
//                    thirdMark = secondMark;
//                    secondMark = firstMark;
//                    firstMark = mark;
//
//                    thirdIndex = secondIndex;
//                    secondIndex = firstIndex;
//                    firstIndex = i;
//                } else if (mark > secondMark) {
//                    thirdMark = secondMark;
//                    secondMark = mark;
//                    thirdIndex = secondIndex;
//                    secondIndex = i;
//                } else if (mark > thirdMark) {
//                    thirdMark = mark;
//                    thirdIndex = i;
//                }
//            }
//            firstURL = url[firstIndex];
//            secondURL = url[secondIndex];
//            thirdURL = url[thirdIndex];

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        isEnd = true;

    }

}
