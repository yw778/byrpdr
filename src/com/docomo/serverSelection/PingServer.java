package com.docomo.serverSelection;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

//import edu.bupt.testinfo.UploadData;

import android.util.Log;

public class PingServer extends Thread {
    String uploadDataURL = "http://xugang.host033.youdnser.com/dataServer/server_selection/insert_server_sel_rank.php";
    final int defaultRank = 15;
    String[] urlArray;
    int finishNumber = 0;
    int[] rank;
    int[] ping;

    double lat;
    double lon;

    String network_type;
    String network_operator;
    String network_standard;

    public PingServer(String[] urlArray, String network_type, String network_operator, String network_standard,
            double lat, double lon) {
        this.urlArray = urlArray;
        this.network_type = network_type;
        this.network_operator = network_operator;
        this.network_standard = network_standard;
        this.lat = lat;
        this.lon = lon;
    }

    public int[] getRank() {
        return rank;
    }

    public int[] getPing() {
        return ping;
    }

    public void run() {
        pingHost ph[] = new pingHost[urlArray.length];
        for (int i = 0; i < urlArray.length; i++) {
            pingHost phi = new pingHost(urlArray[i] + "/latency.txt");
            ph[i] = phi;
            phi.start();
        }

        int wait = 10;
        int finishNumber = 0;
        while (finishNumber < 10 && wait > 0) {
            wait--;
            try {
                sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        ping = new int[urlArray.length];

        for (int j = 0; j < ph.length; j++) {
            if (ph[j].isfinished && ph[j].result != -1) {
                ping[j] = ph[j].result;
            } else
                ping[j] = 65535;

        }

        rank = new int[urlArray.length];
        for (int i = 0; i < urlArray.length; i++) {
            rank[i] = defaultRank;
        }

        for (int i = 0; i < defaultRank; i++) {
            int minPing = 9999999;
            int minIndex = -1;
            for (int j = 0; j < ph.length; j++) {
                if (ph[j].isfinished && ph[j].isSelect == false && ph[j].result != -1 && ph[j].result < minPing) {
                    minPing = ph[j].result;
                    minIndex = j;
                }
            }

            if (minIndex == -1) {
                break;
            } else {
                ph[minIndex].isSelect = true;
                rank[minIndex] = i;
            }
        }

        for (int i = 0; i < rank.length; i++) {
            Log.v("MainActivity", urlArray[i] + ";;ping" + i + ":" + ping[i]);
        }
        insert_rank();

    }

    private class pingHost extends Thread {
        String url;
        public int result = -1;
        public boolean isfinished = false;
        public boolean isSelect = false;

        public pingHost(String url) {
            this.url = url;
        }

        public void run() {
            HttpGet httpRequest = new HttpGet(url);
            httpRequest.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
            try {
                long startTime = System.currentTimeMillis();
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    long stopTime = System.currentTimeMillis();
                    result = (int) (stopTime - startTime);
                } else {
                    result = -1;
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finishNumber++;
            isfinished = true;
        }
    }

    private void insert_rank() {
        String rank_str = null;
        String url_str = null;
        for (int i = 0; i < rank.length; i++) {
            if (i == 0) {
                rank_str = "" + rank[i];
                url_str = urlArray[i];
            } else {
                rank_str += ";" + rank[i];
                url_str += ";" + urlArray[i];
            }
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lat", lat);
            jsonObject.put("lon", lon);
            jsonObject.put("network_type", network_type);
            jsonObject.put("network_operator", network_operator);
            jsonObject.put("network_standard", network_standard);
            jsonObject.put("rank_array", rank_str);
            jsonObject.put("url_array", url_str);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.v("MainActivity", "json:" + jsonObject.toString());
//        UploadData u = new UploadData(uploadDataURL, jsonObject);
//        String re = u.upData();
//        Log.v("MainActivity", "insert_rank_re:" + re);
    }

}
