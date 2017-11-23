package edu.bupt.nettest;

//import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;



import edu.bupt.testinfo.PackData;
import edu.bupt.testinfo.UploadData;
//import edu.bupt.testinfo.PackData;
//import edu.bupt.testinfo.UploadData;
import edu.bupt.unotest.UNOTest;
import android.util.Log;

public class Latency {
    private String pingURL;
    private int timeOut = 5000;
    private String date;
    private UNOTest unoTest;
    private String serverAddress = "buptant.cn/UNOTest/speedtest/latency.txt";
    private String serverAddress2 = "buptant.cn/UNOTest/speedtest/latency.txt";

    native int getLatencyFromJNI();

    native int getState();

    native int setLatencyTime(int threshold);

    native int setLatencyServer(String serverurl);

    native int setLatencyServer2(String serverurl);

    private boolean autoUpload = true;
    int latency;

    public Latency(UNOTest unoTest) {
        this.unoTest = unoTest;
        this.setLatencyServer(serverAddress);
    }

    public boolean setServer(String pingURL) {
        serverAddress = pingURL;
        serverAddress2 = pingURL;
        setLatencyServer(pingURL);
        setLatencyServer2(pingURL);
        return true;
    }

    // multi server
    // x7
    public boolean setServer(String pingURL, String pingURL2) {
        serverAddress = pingURL;
        serverAddress2 = pingURL2;
        setLatencyServer(pingURL);
        setLatencyServer2(pingURL2);
        return true;
    }

    public boolean setTestTime(int timeOut) {
        this.timeOut = timeOut;
        setLatencyTime(timeOut);
        return true;
    }

    public int getLatency() {
        latency = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        date = formatter.format(curDate);
        latency = getLatencyFromJNI();
        Log.v("Latency result", "" + latency);

        unoTest.getInfoUploadTrigger().setLatencyResult(latency);

        new autoUploadThread().start();

        return latency;
    }

    private class autoUploadThread extends Thread {
        public void run() {
            if (autoUpload) {
                PackData packData = new PackData();
                JSONObject latencyjson = packData.packLatencyData(
                        date.toString(), latency,
                        unoTest.locationInfo.getBDLatitude(),
                        unoTest.locationInfo.getBDLongitude(), serverAddress,
                        unoTest.networkInfo.getNetworkType(),
                        unoTest.hardwareInfo.getIMEI(),
                        unoTest.networkInfo.getInternalIP(),
                        unoTest.networkInfo.getExternalIP());
                Log.v("latencytest", latencyjson.toString());
                String latency_result_url = "http://xugang.host033.youdnser.com/serverPHP/updata_latency_db.php";
                UploadData upload_latency_result = new UploadData(
                        latency_result_url, latencyjson);
                String re = upload_latency_result.upData();
                Log.v("latencytest", "" + re);
            }
        }
    }

    public boolean setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
        return true;
    }

    static {
        System.loadLibrary("nettest");
    }

}
