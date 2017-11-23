package edu.bupt.testinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.docomo.Data.TestStatus;

import edu.bupt.unotest.UNOTest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class NetInfo {
    /**
     * 编辑人：林剑辛 时间：2013年3月14日10:17 新建NetInfo类
     * 
     * 获取网络信息 NetInfo(Context) 方法需传入context对象 openNetCard() 打开网卡
     * checkNetWorkState() 检测网络类型 getNetworkId() 获得连接网络的ID getIPAddress() 获得IP地址
     * getMacAddress() 获得MAC地址 getBSSID() 得到接入点的BSSID getWifiInfo()
     * 得到WifiInfo的所有信息 scan() 扫描周边网络 getChannel(int) 获取信道 getScanData() 得到扫描网络结果
     * getAccessWifiInfo() 获取接入Wifi信息 getNearbyWifiInfo() 获取周围Wifi信息
     * getAccessBSInfo()获取接入小区信息 getNearbyBSInfo()获取附近小区信息
     * 
     * 使用： 注意Activity 需创建telephoneManager对象 再传入类中的方法 NetInfo netInfo = new
     * NetInfo(this);//新建BSInfo类并实例化 传入Context对象
     * netInfo.getAccessBSInfo();//使netInfo需要传入telephoneManager对象 获取接入小区信息
     * netInfo.getNearbyBSInfo();//使netInfo需要传入telephoneManager对象 获取附近小区信息
     * 
     * 要获取wifi信息，需要先检测网络状态和打开wifi netInfo.checkNetWorkState();//先检测网络状态
     * netInfo.openNetCard();//打开网卡
     * netInfo.getAccessWifiInfo();//使netInfo的getAccessWifiInfo()方法 获取接入Wifi信息
     * netInfo.getNearbyWifiInfo();//使netInfo的getNearbyWifiInfo() 方法获取周围Wifi信息
     */

    // 定义WifiManager对象
    static WifiManager mWifiManager;
    // 定义WifiInfo对象
    // static WifiInfo mWifiInfo;
    public static boolean ismWifiInfoAviable = false;
    TelephonyManager tm;
    private ServiceState serviceState;
    private static List<ScanResult> listResult;
    private static ScanResult mScanResult;
    private static StringBuffer mStringBuffer = new StringBuffer();
    private static int frequency;
    private static int channel;
    private int present_channel;
    private String present_capabilities;
    public static String alldata;
    private static String ssid;
    private static String sum[][];

    private GetIpInfo getIpInfo;
    private Context context;
    private SignalStrength signalStrength;
    private UNOTest unoTest;
    private String mobilerssi = "";
    private String mobilerssiIndbm = "";
    private String iPinfo = "";

    public NetInfo(Context context, UNOTest unoTest) {
        this.context = context;
        this.unoTest = unoTest;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        serviceState = new ServiceState();
        // if(mWifiInfo!=null)ismWifiInfoAviable=true;
        getIpInfo = new GetIpInfo(getIPAddress() + "");
        getIpInfo.start();

        MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener();
        tm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    public String getSimOperator() {// 获取手机运营商
        return tm.getSimOperator();

    }

    public String getPLMN() {
        return tm.getSimOperator();
    }

    public String getSimOperatorName() {// 获取手机运营商

        String r = "unknown";
        String plmn = tm.getSimOperator();

        Log.w("plmn-getSim", "plmn-getSim " + plmn);
        // switch()
        if (plmn.equals("46000"))
            r = "CMCC";
        else if (plmn.equals("46001"))
            r = "CUCC";
        else if (plmn.equals("46002"))
            r = "CMCC";
        else if (plmn.equals("46008"))
            r = "CMCC";
        else if (plmn.equals("46003"))
            r = "CTCC";
        else if (plmn.equals("46011"))
            r = "CTCC";
        else if (plmn.equals("46006"))
            r = "CUCC";
        else if (plmn.equals("46007"))
            r = "CMCC";
        else if (plmn.equals("46020"))
            r = "CMCC";
        else if (plmn.equals("46005"))

            r = "CTCC";
        else
            r = tm.getSimOperator();
        return r;
    }

    public static int getIPAddress() { // ?
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        if (mWifiInfo == null)
            return 0;
        else {
            // Log.v("NetInfo","getIPAddress ="+mWifiInfo.getIpAddress());
            return mWifiInfo.getIpAddress();
        }
    }

    // 得到MAC地址
    public static String getWifiMacAddress() {
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();

    }

    // 得到接入点的BSSID
    public String getWifiBSSID() {
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public String getCellID() {
        String cid = "unknown";
        switch (tm.getPhoneType()) {
        case 1: {// G 网
            GsmCellLocation gsm = (GsmCellLocation) tm.getCellLocation();
            try {
                cid = "-" + gsm.getLac() + "-" + gsm.getCid();
            } catch (Exception e) {
                e.toString();
            }
            break;
        }
        case 2: {// C网
            try {
                CdmaCellLocation cdma = (CdmaCellLocation) tm.getCellLocation();
                cid = "-" + cdma.getSystemId() + "-" + cdma.getNetworkId() + "-" + cdma.getBaseStationId();
            } catch (Exception e) {
                e.toString();
            }
            break;
        }
        }
        return tm.getNetworkOperator().toString() + cid;
    }

    // 得到WifiInfo的所有信息包
    /*
     * public String getWifiInfo() { return (mWifiInfo == null) ? "NULL" :
     * mWifiInfo.toString(); }
     */

    public JSONObject getAccessWifiInfo() {// 得到WifiInfo的信息包，打包成json
        JSONObject json = new JSONObject();
        if (mWifiManager.isWifiEnabled()) {
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiInfo != null) {
                /***
                 * 获取当前连接wifi的channel
                 */
                mWifiManager.startScan();
                listResult = mWifiManager.getScanResults();
                try {
                    if (listResult != null) {
                        for (int i = 0; i < listResult.size(); i++) {
                            mScanResult = listResult.get(i);
                            if (mWifiInfo.getBSSID().equals(mScanResult.BSSID)) {
                                present_channel = getChannel(mScanResult.frequency);
                                present_capabilities = mScanResult.capabilities;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {

                    json.put("mac_addr", mWifiInfo.getMacAddress());
                    json.put("channel", present_channel);
                    json.put("link_speed", mWifiInfo.getLinkSpeed());
                    json.put("wifi_bss_id", mWifiInfo.getBSSID());
                    json.put("gps_lat", "" + unoTest.locationInfo.getBDLatitude());
                    json.put("gps_lon", "" + unoTest.locationInfo.getBDLongitude());
                    json.put("wifi_name", mWifiInfo.getSSID());
                    json.put("encyption_type", "" + mWifiInfo.getSupplicantState());
                    json.put("cipher_mode", "" + present_capabilities);// modified
                                                                       // by
                                                                       // deng
                    json.put("version", TestStatus.Con_version);// modified by
                                                                // deng
                                                                // 2013.6.25
                    json.put("detail", TestStatus.locationTag);
                    json.put("location_type",
                            unoTest.locationInfo.getProviderName() + unoTest.locationInfo.getBaiDuCoorType());

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
            return null;
        }
        return json;
    }

    /**
     * 得到周边wifi结果
     */
    public JSONObject getNearbyWifiInfo() {
        JSONArray jsonarray;
        JSONObject jsonobject;
        if (mWifiManager.isWifiEnabled()) {

            jsonobject = new JSONObject();

            // 每次点击扫描之前清空上一次的扫描结果
            if (mStringBuffer != null) {
                mStringBuffer = new StringBuffer();
            }
            // 开始扫描网络
            mWifiManager.startScan();
            listResult = mWifiManager.getScanResults();
            if (listResult != null) {
                Log.i("wifiscan", "当前区域存在无线网络，请查看扫描结果");
                jsonarray = new JSONArray();
                try {
                    Log.i("size", "" + listResult.size());
                    for (int i = 0; i < listResult.size(); i++) {
                        mScanResult = listResult.get(i);
                        frequency = mScanResult.frequency;
                        channel = getChannel(frequency);
                        JSONObject Json = new JSONObject();
                        Json.put("ssid", mScanResult.SSID);
                        Json.put("bssid", mScanResult.BSSID);
                        Json.put("level", mScanResult.level);
                        Json.put("frequency", mScanResult.frequency);
                        Json.put("channel", channel);
                        Json.put("describecontents", mScanResult.describeContents());
                        Json.put("capabilities", mScanResult.capabilities);

                        // Log.w("present_channel","present_channel capabilities="+
                        // mScanResult.capabilities);
                        jsonarray.put(i, Json);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("wifiscan", "当前区域没有无线网络");
                return null;
            }
            try {
                jsonobject.put("detail", TestStatus.locationTag);// modified by
                                                                 // deng
                                                                 // 2013-7-15
//                jsonobject.put("location_type",
//                        unoTest.locationInfo.getProviderName() + unoTest.locationInfo.getBaiDuCoorType());
//                jsonobject.put("gps_lat", "" + unoTest.locationInfo.getBDLatitude());
//                jsonobject.put("gps_lon", "" + unoTest.locationInfo.getBDLongitude());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                String date = formatter.format(curDate);
                jsonobject.put("time_index", date);
                jsonobject.put("wifi_list", jsonarray.toString());
                jsonobject.put("version", TestStatus.Con_version);// modified by
                                                                  // deng
                                                                  // 2013.6.25

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return jsonobject;

    }

    /**
     * 根据频率获得信道
     * 
     * @param frequency
     * @return
     */
    private static int getChannel(int frequency) {
        int channel = 0;
        switch (frequency) {
        case 2412:
            channel = 1;
            break;
        case 2417:
            channel = 2;
            break;
        case 2422:
            channel = 3;
            break;
        case 2427:
            channel = 4;
            break;
        case 2432:
            channel = 5;
            break;
        case 2437:
            channel = 6;
            break;
        case 2442:
            channel = 7;
            break;
        case 2447:
            channel = 8;
            break;
        case 2452:
            channel = 9;
            break;
        case 2457:
            channel = 10;
            break;
        case 2462:
            channel = 11;
            break;
        case 2467:
            channel = 12;
            break;
        case 2472:
            channel = 13;
            break;
        case 2484:
            channel = 14;
            break;
        default:
            break;
        }
        return channel;
    }

    public JSONObject getAccessBSInfo() throws JSONException {
        int PhoneType;

        PhoneType = tm.getPhoneType();// 获取手机类型 GSM 1 CDMA 2
        Log.v("phonetype", "" + PhoneType);
        JSONObject json = new JSONObject();
        String cid = "unknown";
        String cell_id = "";
        int lac = -1;
        switch (PhoneType) {
        case 1: {
            GsmCellLocation gsm = (GsmCellLocation) tm.getCellLocation();
            try {
                json.put("network_standard", getNetworkStandard(tm.getNetworkType()));
                cid = "" + gsm.getCid();
                json.put("cid", cid);
                lac = gsm.getLac();
                json.put("lac", gsm.getLac());
                json.put("psc", gsm.getPsc());
                cell_id = "-" + gsm.getLac() + "-" + cid;
                // if(signalStrength.isGsm()){
                // json.put("rssi", signalStrength.getGsmSignalStrength());
                // }else{
                //
                // }
                // json.put("rssi",signalStrength.getGsmSignalStrength()+"");
                json.put("networkid", "-1");
                json.put("systemid", "-1");
            } catch (Exception e) {
                e.toString();
            }
            break;
        }
        case 2: {

            try {
                CdmaCellLocation cdma = (CdmaCellLocation) tm.getCellLocation();
                Log.d("CdmaCellLocation", cdma.toString());
                json.put("network_standard", getNetworkStandard(tm.getNetworkType()));
                json.put("lac", "-1");
                json.put("psc", "-1");
                json.put("cid", "" + cdma.getBaseStationId());
                json.put("networkid", cdma.getNetworkId());
                json.put("systemid", cdma.getSystemId());
                cell_id = "-" + cdma.getSystemId() + "-" + cdma.getNetworkId() + "-" + cdma.getBaseStationId();
            } catch (Exception e) {
                e.toString();
            }
            break;
        }
        }

        try {
            // json.put("deviceid", tm.getDeviceId());
            json.put("operator_name", getSimOperatorName());
            // json.put("operator", tm.getNetworkOperator());
            String mccMnc = tm.getNetworkOperator();
            json.put("plmn", tm.getNetworkOperator().toString());
            // Log.v("plmn",tm.getNetworkOperator().toString());
            // if(mccMnc!=null&&mccMnc.length()>=5){
            // json.put("mcc", tm.getNetworkOperator().substring(0,3));
            // //MCC：Mobile Country Code，移动国家码，共3位，中国为460;
            // json.put("mnc", tm.getNetworkOperator().substring(3,5));
            // //MNC:Mobile NetworkCode，移动网络码，共2位
            // }else{
            // json.put("mcc", "-1"); //MCC：Mobile Country
            // Code，移动国家码，共3位，中国为460;
            // json.put("mnc", "-1"); //MNC:Mobile NetworkCode，移动网络码，共2位
            // }
            json.put("network_type", "" + getNetworkGeneration());
            json.put("cell_id", tm.getNetworkOperator().toString() + cell_id);
            json.put("version", TestStatus.Con_version);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;
    }

    private int getgetAccessBSRssi() {
        MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener();
        tm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        return -1;
    }

    private class MyPhoneStateListener extends PhoneStateListener // 有待于测试
    {
        @Override
        public void onSignalStrengthsChanged(SignalStrength getsignalStrength) {
            super.onSignalStrengthsChanged(getsignalStrength);
            signalStrength = getsignalStrength;
            if (signalStrength.isGsm()) {
                mobilerssi = signalStrength.getGsmSignalStrength() + "";
                int asuTodbm = 2 * Integer.parseInt(mobilerssi) - 113;
                mobilerssiIndbm = String.valueOf(asuTodbm) + "dbm";
            } else {
                mobilerssi = signalStrength.getEvdoDbm() + "dbm";
                mobilerssiIndbm = signalStrength.getEvdoDbm() + "dbm";
            }
            // Log.v("isGsm",""+signalStrength.isGsm());
            // Log.v("aaaaaaaa",""+signalStrength.toString());
            // Log.v("getCdmaDbm",""+signalStrength.getCdmaDbm());
            // //Log.v("getCdmaEcio",""+signalStrength.getCdmaEcio());
            // Log.v("getEvdoDbm",""+signalStrength.getEvdoDbm());
            // //Log.v("getEvdoEcio",""+signalStrength.getEvdoEcio());
            // Log.v("getEvdoSnr",""+signalStrength.getEvdoSnr());
            // Log.v("getGsmBitErrorRate",""+signalStrength.getGsmBitErrorRate());
            // Log.v("getGsmSignalStrength",""+signalStrength.getGsmSignalStrength());

        }
    };

    public String getRssi() {
        return mobilerssi;
    }

    public String getRssiInDbm() {
        return mobilerssiIndbm;
    }

    public String getWifiRssi() {
        if (mWifiManager.isWifiEnabled()) {
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiInfo != null) {
                return mWifiInfo.getRssi() + "";
            }
        }
        return null;
    }

    private String getNetworkStandard(int networkType) {
        String networktypestring = "";
        switch (networkType) {
        case 0:
            networktypestring = "mobile";
            break;
        case 1:
            networktypestring = "GPRS";
            break;// 2G
        case 2:
            networktypestring = "EDGE";
            break;// 2G
        case 3:
            networktypestring = "UMTS";
            break;// 3G
        case 4:
            networktypestring = "CDMA";
            break;// 2G
        case 5:
            networktypestring = "EVDO_0";
            break;// 3G
        case 6:
            networktypestring = "EVDO_A";
            break;// 3G
        case 7:
            networktypestring = "1xRTT";
            break;// 2G
        case 8:
            networktypestring = "HSDPA";
            break;// 3G
        case 9:
            networktypestring = "HSUPA";
            break;// 3G
        case 10:
            networktypestring = "HSPA";
            break;// 3G
        case 11:
            networktypestring = "IDEN";
            break;// 2G
        case 12:
            networktypestring = "EVDO_B";
            break;// 3G
        case 13:
            networktypestring = "LTE";
            break;// 4G
        case 14:
            networktypestring = "eHRPD";
            break;// 3G
        case 15:
            networktypestring = "HSPA+";
            break;// 3G

        default:
            networktypestring = "mobile";
            break;
        }
        return networktypestring;
    }

    /**
     * return the network standard, such as edge,umts,cdma,hsdpa....
     * */
    public String getNetworkStandard() {
        String networktypestring = "";
        switch (tm.getNetworkType()) {
        case 0:
            networktypestring = "mobile";
            break;
        case 1:
            networktypestring = "GPRS";
            break;// 2G
        case 2:
            networktypestring = "EDGE";
            break;// 2G
        case 3:
            networktypestring = "UMTS";
            break;// 3G
        case 4:
            networktypestring = "CDMA";
            break;// 2G
        case 5:
            networktypestring = "EVDO_0";
            break;// 3G
        case 6:
            networktypestring = "EVDO_A";
            break;// 3G
        case 7:
            networktypestring = "1xRTT";
            break;// 2G
        case 8:
            networktypestring = "HSDPA";
            break;// 3G
        case 9:
            networktypestring = "HSUPA";
            break;// 3G
        case 10:
            networktypestring = "HSPA";
            break;// 3G
        case 11:
            networktypestring = "IDEN";
            break;// 2G
        case 12:
            networktypestring = "EVDO_B";
            break;// 3G
        case 13:
            networktypestring = "LTE";
            break;// 4G
        case 14:
            networktypestring = "eHRPD";
            break;// 3G
        case 15:
            networktypestring = "HSPA+";
            break;// 3G

        default:
            networktypestring = "mobile";
            break;
        }
        return networktypestring;

    }

    public String getNetworkGeneration() {
        String networktypestring = "";
        int networkType = tm.getNetworkType();
        switch (networkType) {
        case 0:
            networktypestring = "mobile";
            break;
        case 1:
            networktypestring = "2G";
            break;// 2G
        case 2:
            networktypestring = "2G";
            break;// 2G
        case 3:
            networktypestring = "3G";
            break;// 3G
        case 4:
            networktypestring = "2G";
            break;// 2G
        case 5:
            networktypestring = "3G";
            break;// 3G
        case 6:
            networktypestring = "3G";
            break;// 3G
        case 7:
            networktypestring = "2G";
            break;// 3G
        case 8:
            networktypestring = "3G";
            break;// 3G
        case 9:
            networktypestring = "3G";
            break;// 3G
        case 10:
            networktypestring = "3G";
            break;// 3G
        case 11:
            networktypestring = "2G";
            break;// 2G
        case 12:
            networktypestring = "3G";
            break;// 3G
        case 13:
            networktypestring = "4G";
            break;// 4G
        case 14:
            networktypestring = "3G";
            break;// 4G
        case 15:
            networktypestring = "3G";
            break;// 4G
        default:
            networktypestring = "mobile";
            break;
        }
        return networktypestring;
    }

    public JSONObject getNearbyBSInfo() {// 获取附近的小区信息
        JSONArray jsonarray;
        JSONObject jsonobject;
        List<NeighboringCellInfo> infos = tm.getNeighboringCellInfo();
        Log.d("infos.size", "infos.size" + infos.size());
        Log.d("infos", "infos" + infos.toString());
        if (infos.size() > 0) {
            jsonobject = new JSONObject();
            jsonarray = new JSONArray();
            int i = 0;
            for (NeighboringCellInfo info : infos) {
                // 获取邻居小区号
                JSONObject json = new JSONObject();
                try {
                    json.put("cid", info.getCid());
                    json.put("lac", info.getLac());
                    json.put("networkstardard", getNetworkStandard(tm.getNetworkType()));
                    json.put("psc", info.getPsc());
                    json.put("rssi", info.getRssi());
                    jsonarray.put(i, json);
                    i++;
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 获取邻居小区LAC，LAC:
                // 位置区域码。为了确定移动台的位置，每个GSM/PLMN的覆盖区都被划分成许多位置区，LAC则用于标识不同的位置区。
            }
            try {
//                jsonobject.put("gps_lat", "" + unoTest.locationInfo.getBDLatitude());
//                jsonobject.put("gps_lon", "" + unoTest.locationInfo.getBDLongitude());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                String date = formatter.format(curDate);
                jsonobject.put("time_index", date);
                jsonobject.put("cell_list", jsonarray.toString());
                jsonobject.put("version", TestStatus.Con_version);// modified by
                                                                  // dengxihai
                                                                  // 2013.6.25
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Log.d("infos",json.toString());
        } else {
            return null;
        }
        return jsonobject;

    }

    /**
     * getIpinfo
     * */
    public String getTheIpinfo() {
        if (getIpInfo.ipinfo.equals("网络查询ip失败") || getIpInfo.ipinfo.startsWith("<")) {
            getIpInfo = new GetIpInfo(getIPAddress() + "");// 如果获取失败，再获取一次
            getIpInfo.start();
        }
        return getIpInfo.ipinfo;

    }

    class GetIpInfo extends Thread {
        private String ip = "10.105.38.247";// /初始化的ip
        public String ipinfo = "正在查询";// /ip的信息
        private String url = "http://whois.pconline.com.cn/ip.jsp/";// 一个免费的查询

        public GetIpInfo(String infoIp) {
            this.ip = infoIp;
        }

        public void run() {
            try {
                HttpResponse httpResponse = null;
                url += "?ip=" + ip;
                HttpGet httpGet = new HttpGet(url);
                httpResponse = new DefaultHttpClient().execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    ipinfo = EntityUtils.toString(httpResponse.getEntity());
                    ipinfo = ipinfo.replaceAll("\r", "");
                    ipinfo = ipinfo.replaceAll("\n", "");
                    ipinfo = ipinfo.replace(" ", "");
                    // Log.v("NetInfo","GetIpInfo ipinfo ="+ipinfo);
                }
            } catch (Exception x) {
                ipinfo = "网络查询ip失败";
                x.printStackTrace();
            }

        }

    }

    public String getInternalIP() {// get Internal IP Addr
        Enumeration en;
        Enumeration enumIpAddr;
        String ip = "";
        try {

            for (en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {

                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String s1 = ".";
                        String s2 = ":";
                        if (inetAddress.getHostAddress().toString().contains(s1))
                            ip += "ipv4:" + inetAddress.getHostAddress().toString() + "\n";
                        if (inetAddress.getHostAddress().toString().contains(s2))
                            ip += "ipv6:" + inetAddress.getHostAddress().toString() + "\n";
                    }
                }
            }

        } catch (SocketException ex) {
            Log.w("lin", "lin test" + ex.toString());

        }

        if (ip != null)
            return ip;
        else
            return null;

    }

    public String getExternalIP() {// Get External IP Addr
        String ipChange = "http://iframe.ip138.com/ic.asp";
        URL infoUrl = null;
        InputStream inStream = null;
        try {
            infoUrl = new URL(ipChange);
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                int start = strber.indexOf("[");
                int end = strber.indexOf("]", start + 1);
                line = strber.substring(start + 1, end);
                return line;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "null";
    }

    public Boolean isNetworkSmooth() {
        int status = -1;
        try {
            Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + "www.sina.com.cn");
            status = p.waitFor();
            if (status == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // //////////////////////////////////获取网络类型/////////////////////////////////////
    // need to be tested

    public String getNetworkType() {
        String networkType = "unavailabe";
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        // 获取代表联网状态的NetWorkInfo对象
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        // 获取当前的网络连接是否可用
        if (networkInfo == null) {
            Log.i("网络状态通知", "当前的网络连接不可用");
        } else {
            boolean available = networkInfo.isAvailable();
            if (available) {

            } else {

                Log.i("网络状态通知", "当前的网络连接不可用");
            }
        }

        try {
            State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (State.CONNECTED == state) {
                networkType = this.getNetworkGeneration();
            }
        } catch (Exception e) {

        }
        try {
            State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (State.CONNECTED == state) {
                networkType = "Wi-Fi";
            }
        } catch (Exception e) {

        }
        // Log.v("NetInfo","networkTypes"+networkType);
        return networkType;
    }
}
