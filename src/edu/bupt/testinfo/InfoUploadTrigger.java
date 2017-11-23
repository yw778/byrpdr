package edu.bupt.testinfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.docomo.Data.FileStore;
import com.docomo.Data.TestStatus;
import com.docomo.serverSelection.InsertServerDownload;

import edu.bupt.unotest.UNOTest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class InfoUploadTrigger {

    // Q:如何保证ping不通继续Ping 如何保证只触发一次上传
    private Context context;
    IntentFilter intentFilter;
    private int tryTimes = 10; // 尝试上传次数
    private Boolean isUploading = false;

    String TAG = "InfoUploadTrigger";
    private UNOTest unoTest;

    private int ave_download_speed = 0;
    private int max_download_speed = 0;
    private int ave_upload_speed = 0;
    private int max_upload_speed = 0;
    private int ping_latency = 0;
    private int serverone_ave_download_speed = 0;
    private int servertwo_ave_download_speed = 0;
    private String serverone_addr = "";
    private String servertwo_addr = "";

    private FileStore filestore;

    private boolean isWifiAvailable = false;

    // 校园测试测试版本，isControlUpload=true时每次测试完成上传测试数据

    public InfoUploadTrigger(Context context, UNOTest unoTest) {
        this.context = context;
        this.unoTest = unoTest;
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         intentFilter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
         intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(connectionReceiver, intentFilter);

        filestore = new FileStore(context);

    }

    public boolean setLatencyResult(int ping_latency) {
        this.ping_latency = ping_latency;
        return true;
    }

    public boolean setServerOneResult(int serverOne_speed) {
        this.serverone_ave_download_speed = serverOne_speed;
        return true;
    }

    public boolean setServerTwoResult(int serverTwo_speed) {
        this.servertwo_ave_download_speed = serverTwo_speed;
        return true;
    }

    public boolean setMultiServerAddr(String one, String two) {
        int temp = one.lastIndexOf("/");
        serverone_addr = one.substring(0, temp);
        temp = two.lastIndexOf("/");
        servertwo_addr = two.substring(0, temp);

        return true;
    }

    public boolean setDownloadResult(int ave_download_speed, int max_download_speed) {
        this.ave_download_speed = ave_download_speed;
        this.max_download_speed = max_download_speed;
        return true;
    }

    public boolean setUploadResult(int ave_upload_speed, int max_upload_speed) {
        this.ave_upload_speed = ave_upload_speed;
        this.max_upload_speed = max_upload_speed;
        return true;
    }

    public void startInfoUploadTrigger() {
        if (!isUploading) {
            isUploading = true;
            if (isUploadNeeded()) {
                // 执行上传

                handler.post(runnable);
                Log.v("UploadNetworkStat", "需要上传");
            } else {
                isUploading = false;
                Log.v("UploadNetworkStat", "不需要上传");
            }
        }
    }

    private boolean isUploadNeeded() {
        // 检查是否需要上传
        return true;
    }

    public void uploadInfo() {

        if (isWifiAvailable || TestStatus.isUploadInstant) {
            JSONObject hwjson = unoTest.hardwareInfo.getHwInfo();
            String hwInfoUrl = "http://xugang.host033.youdnser.com/dataServer/insert_device_db.php";
            String hwInfoResult = uploadToServer(hwInfoUrl, hwjson);
            Log.v(TAG, "硬件信息上传成功   " + hwInfoResult.toString());
        }

        try {
            JSONObject celljson = unoTest.networkInfo.getAccessBSInfo();
            if (celljson != null) {
                if (isWifiAvailable || TestStatus.isUploadInstant) {
                    String cellInfoUrl = "http://xugang.host033.youdnser.com/dataServer/insert_cell_db.php";
                    String cellInfoResult = uploadToServer(cellInfoUrl, celljson);
                    Log.v(TAG, "附着基站信息上传成功   " + cellInfoResult.toString());
                } else {
                    filestore.Store(FileStore.tablename_cell, celljson);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            JSONObject cellscanjson = unoTest.networkInfo.getNearbyBSInfo();
            if (cellscanjson != null) {
                if (isWifiAvailable || TestStatus.isUploadInstant) {
                    String cellscanInfoUrl = "http://xugang.host033.youdnser.com/dataServer/insert_cell_scan_db.php";
                    String cellscanInfoResult = uploadToServer(cellscanInfoUrl, cellscanjson);
                    Log.v(TAG, "周边基站信息上传成功  " + cellscanjson.toString());
                } else {
                    filestore.Store(FileStore.tablename_cell_scan, cellscanjson);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            JSONObject wifijson = unoTest.networkInfo.getAccessWifiInfo();
            if (wifijson != null) {
                if (isWifiAvailable || TestStatus.isUploadInstant) {
                    String wifiInfoUrl = "http://xugang.host033.youdnser.com/dataServer/insert_wifi_db.php";
                    String wifiInfoResult = uploadToServer(wifiInfoUrl, wifijson);
                    Log.v(TAG, "接入wifi信息上传成功  " + wifiInfoResult.toString());
                    Log.w(TAG, "接入wifi信息  " + wifijson.toString());
                } else {
                    filestore.Store(FileStore.tablename_wifi, wifijson);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            JSONObject wifiscanjson = unoTest.networkInfo.getNearbyWifiInfo();
            if (wifiscanjson != null) {
                if (isWifiAvailable || TestStatus.isUploadInstant) {
                    String wifiscanInfoUrl = "http://xugang.host033.youdnser.com/dataServer/insert_gps_wifi_db.php";
                    String wifiscanInfoResult = uploadToServer(wifiscanInfoUrl, wifiscanjson);
                    Log.v(TAG, "周边wifi信息上传成功1   " + wifiscanInfoResult.toString());
                    Log.w(TAG, "周边wifi信息 2 " + wifiscanjson.toString());
                } else {
                    filestore.Store(FileStore.tablename_gps_wifi, wifiscanjson);
                    Log.v(TAG, "周边wifi上传成功  3" + wifiscanjson.toString());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ping", ping_latency);
            jsonObject.put("ave_downloadSpeed", "" + ave_download_speed);
            jsonObject.put("max_downloadSpeed", "" + max_download_speed);
            jsonObject.put("ave_uploadSpeed", "" + ave_upload_speed);
            jsonObject.put("max_uploadSpeed", "" + max_upload_speed);
            jsonObject.put("gps_lat", "" + unoTest.locationInfo.getBDLatitude());
            jsonObject.put("gps_lon", "" + unoTest.locationInfo.getBDLongitude());
            jsonObject.put("ant_version", TestStatus.Con_version);
            jsonObject.put("detail", TestStatus.locationTag);
            jsonObject.put("location_type",
                    unoTest.locationInfo.getProviderName() + unoTest.locationInfo.getBaiDuCoorType());

            jsonObject.put("server_url", unoTest.serverInfo.getTestServers());

            jsonObject.put("networkType", unoTest.networkInfo.getNetworkType());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String date = formatter.format(curDate);
            jsonObject.put("time_client_test", date);
            jsonObject.put("operator_name", unoTest.networkInfo.getSimOperatorName());
            jsonObject.put("imei", "" + unoTest.hardwareInfo.getIMEI());
            jsonObject.put("cell_id", "" + unoTest.networkInfo.getCellID());

            if (unoTest.networkInfo.getNetworkType() == "Wi-Fi") {
                jsonObject.put("wifi_bss_id", unoTest.networkInfo.getWifiBSSID());
                jsonObject.put("rssi", unoTest.networkInfo.getWifiRssi());
            } else {
                jsonObject.put("wifi_bss_id", "null");
                jsonObject.put("rssi", unoTest.networkInfo.getRssi());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        

        if (isWifiAvailable || TestStatus.isUploadInstant) {
//            String testInfoUrl = "http://xugang.host033.youdnser.com/dataServer/insert_testinfo_db.php";
			String testInfoUrl = "http://buptant.cn/anttest/applist-traffic-testinfo/insert_testinfo_db.php";
        	String testInfoResult = uploadToServer(testInfoUrl, jsonObject);            
            Log.v(TAG, "测试信息上传成功 " + testInfoResult);
        } else {
            filestore.Store(FileStore.tablename_testinfo, jsonObject);

        }
        //服务器信息
        if (isWifiAvailable || TestStatus.isUploadInstant) {
            InsertServerDownload i = new InsertServerDownload("w", unoTest.networkInfo.getExternalIP(),
                    unoTest.networkInfo.getTheIpinfo(), serverone_addr, serverone_ave_download_speed,
                    unoTest.locationInfo.getBDLatitude(), unoTest.locationInfo.getBDLongitude());
            i.start();
            InsertServerDownload j = new InsertServerDownload("w", unoTest.networkInfo.getExternalIP(),
                    unoTest.networkInfo.getTheIpinfo(), servertwo_addr, servertwo_ave_download_speed,
                    unoTest.locationInfo.getBDLatitude(), unoTest.locationInfo.getBDLongitude());
            j.start();
        } else {
            InsertServerDownload i = new InsertServerDownload("m", unoTest.networkInfo.getPLMN(),
                    unoTest.networkInfo.getNetworkStandard(), serverone_addr, serverone_ave_download_speed,
                    unoTest.locationInfo.getBDLatitude(), unoTest.locationInfo.getBDLongitude());
            i.start();
            InsertServerDownload j = new InsertServerDownload("m", unoTest.networkInfo.getPLMN(),
                    unoTest.networkInfo.getNetworkStandard(), servertwo_addr, servertwo_ave_download_speed,
                    unoTest.locationInfo.getBDLatitude(), unoTest.locationInfo.getBDLongitude());
            j.start();

        }

    }

    private String uploadToServer(String serverurl, JSONObject json) {
        UploadData upload_info_result = new UploadData(serverurl, json);
        String re = upload_info_result.upData();
        Log.v(TAG,"yyl"+ re);
        return re;
    }

    class uploadThread extends Thread {
        public void run() {
            Log.v("UploadNetworkStat", "执行上传");
            filestore.uploadStore();
        }
    }

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {

        public void run() {
            new pingThread().start();
        }

    };

    class pingThread extends Thread {
        public void run() {
            Log.v("UploadNetworkStat", "尝试上传");
            int status = -1;
            try {
                Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + "www.sina.com.cn");
                status = p.waitFor();
                if (status == 0) {
                    isWifiAvailable = true;
                    new uploadThread().start();
                } else {
                    isWifiAvailable = false;
                    if (tryTimes > 0) {
                        tryTimes--;
                        handler.postDelayed(runnable, 100);
                        Log.v("UploadNetworkStat", "剩余尝试次数" + tryTimes);
                    } else {
                        tryTimes = 10;
                        isUploading = false;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.v("UploadNetworkStat", "" + status);
        }
    }

    public boolean destroyInfoUploadTrigger() { // 注意一定要取消监听
        context.unregisterReceiver(connectionReceiver);
        return true;
    }

    private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                Log.v(TAG, "NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo networkInfo = intent.getParcelableExtra("networkInfo");
                if (networkInfo.getType() == 1) {
                    // Log.v(TAG, "NetworkInfo wifi");
                    if (networkInfo.getState() == State.CONNECTED) {
                        // Log.v(TAG, "NetworkInfo wifi connected");
                        startInfoUploadTrigger();
                    }
                }
            }
        }
    };
}
