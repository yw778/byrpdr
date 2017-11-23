package edu.bupt.testinfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.docomo.Data.TestStatus;

import android.app.ActivityManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class HardwareInfo {

    private static TelephonyManager tm;
    private Context context;

    public HardwareInfo(Context context) {// 构造函数，实例化HardwareInfo对象

        this.context = context;
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // Log.d("hardwareinfo","initial");
    }

    public JSONObject getHwInfo() {
        // 得到WifiInfo的信息包，打包成json
        JSONObject json = new JSONObject();
        try {
            json.put("cpu", this.getCpuInfo());
            // json.put("memory", ""+this.getTotalMemory());
            json.put("os", "android");
            json.put("imei", this.getIMEI());
            json.put("model", this.getBuildModel());
            json.put("sdk_version", this.getSDKVersion());
            json.put("os_version", this.getOSVersion());
            json.put("company", this.getPhoneCompany());
            // modified by dengxihai 2013/6/16
            json.put("display", this.getDisplaySize());
            json.put("total_memory", "" + this.getTotalMemory());
            json.put("free_memory", getFreeMemory(context));
            json.put("version", TestStatus.Con_version);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json;
    }

    public String getIMEI() { // 获取IMEI硬件
        // Log.d("IMEI",tm.getDeviceId());
        return tm.getDeviceId();

    }

    public String getCpuInfo() { // 获取CPU硬件
        String str1 = "/proc/cpuinfo";
        String str2 = "";

        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split(":");
            // Log.d("CPU",arrayOfString[1]);
            str2 = arrayOfString[1];
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return str2;
    }

    public String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split(" +");
            // Log.v("arrayOfString",arrayOfString.toString());
            for (String num : arrayOfString) {
                // Log.i(str2, num);
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }

        String memory = initial_memory + "kB";
        return memory; // Formatter.formatFileSize(context, initial_memory);//
                       // Byte转换为KB或者MB，内存大小规格化
    }

    public String getFreeMemory(Context mContext) {
        long MEM_UNUSED;
        // 得到ActivityManager
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        // 创建ActivityManager.MemoryInfo对象
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // 取得剩余的内存空间
        MEM_UNUSED = mi.availMem / 1024;
        return "" + MEM_UNUSED + "KB";
    }

    public String getOSVersion() {// 获取手机系统

        String OS = "";
        OS = android.os.Build.VERSION.RELEASE;
        return OS;

    }

    public String getPhoneCompany() {// 获取手机公司
        String MANUFACTURER = "";
        MANUFACTURER = android.os.Build.MANUFACTURER;
        if (MANUFACTURER == null)
            MANUFACTURER = "unknown";
        // MANUFACTURER = MANUFACTURER.replaceAll(" ","-");
        // Log.d("MANUFACTURER",MANUFACTURER);
        return MANUFACTURER;

    }

    public String getBuildModel() {// 软件

        String MODEL = "";
        MODEL = android.os.Build.MODEL;
        // MODEL = MODEL.replaceAll(" ","-");
        return MODEL;

    }

    public String getReleaseVersion() {// 软件

        String RELEASE = "";
        RELEASE = android.os.Build.VERSION.RELEASE;
        // RELEASE = RELEASE.replaceAll(" ","-");
        return RELEASE;

    }

    public String getSDKVersion() {// 软件

        String SDK = "";
        SDK = android.os.Build.VERSION.SDK;
        // SDK = SDK.replaceAll(" ","-");
        return SDK;

    }

    /**
     * 参看 http://www.cnblogs.com/renyuan/archive/2012/07/25/2607936.html
     * manifest中需要配置 应用程序可使用的屏幕 像素 点 大小【比如联想A798t下边的虚拟 按键 是不计入的】
     * */
    public String getDisplaySize() {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        int screenHeight = dm.heightPixels; // 屏幕高（像素，如：800px）
        String result = screenWidth + "*" + screenHeight;
        // Log.w("Display==","Display=="+result);
        return result;
    }

}
