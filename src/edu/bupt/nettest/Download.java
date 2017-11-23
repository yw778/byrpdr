//Download.java
//Created by x7, Mar 12, 2013
// Modified by x7, Mar 15, 2013

package edu.bupt.nettest;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;




import edu.bupt.testinfo.PackData;
import edu.bupt.testinfo.UploadData;
import edu.bupt.unotest.UNOTest;
import android.util.Log;

/** Part of test API, download test */


public class Download {
    public native int startFromJNI();

    public native void stopFromJNI();

    public native int getCurrentSpeed();

    public native int getAveSpeed();

    public native int getMaxSpeed();

    private native int getTestState();
    
    public native int getServerOneAveSpeed();
    
    public native int getServerTwoAveSpeed();
    

    

    public native void setThreadNum(int num);

    private native void setServer(String serverAddress);
    
    private native void setMultiServer(String serverOne,String serverTwo);

    public native void setDuration(int duration);

    public native void setFrequency(int frequency);
    
    private native double getDropRatio();
    
    private native void setTrafficDuration(int trafficduration);

    private int frequency = 500; // ms
    
    private UNOTest unoTest;  
    
    private String date;
    private String serverOneAddr,serverTwoAddr;
    
    private String serverAddress = "buptant.cn/UNOTest/speedtest/random350x350.jpg";
    
    public Download(UNOTest unoTest){
        this.unoTest = unoTest;	
        this.setServer(serverAddress);
    }
    
    public void setTrafficLimition(int trafficduration){
    	setTrafficDuration(trafficduration);
    }
    
    public boolean setTestServer(String serverAddress) {
        this.serverAddress = serverAddress.substring(7);
        this.setServer(serverAddress);
        Log.v("Download","serverAddress:"+serverAddress);
        return true;
    }
    
    public boolean setMultiTestServer(String serverOne,String serverTwo){
    	serverOneAddr = serverOne;
    	serverTwoAddr = serverTwo;
    	setMultiServer(serverOne.substring(7), serverTwo.substring(7));
    	return true;
    }
    
    public  String getServerOneAddress(){
    	return serverOneAddr;
    }
    
    public  String getServerTwoAddress(){
    	return serverTwoAddr;
    }

    public void setCalFrequency(int frequency) {
        this.frequency = frequency;
        setFrequency(frequency);
    }

    public native int updateTraffic();

    static {
        System.loadLibrary("nettest");
    }
    /** final variables */
    public static final int STATE_READY = 0x0; // states of test
    public static final int STATE_STARTED = 0x1; //
                                                 // Modified by x7, Mar 15, 2013
    public static final int STATE_RUNNING = 0x2;
    public static final int STATE_COMPLETE = 0x3;
    public static final int STATE_KILLED = 0x4;
    private final String DEBUGTAG = "download";

    private boolean autoUpload = true; // whether to upload the test data to our
                                       // server
    private int state = Download.STATE_READY; // state of the test

    public void start() {
        state = Download.STATE_RUNNING; // set state
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        date = formatter.format(curDate);
        setCalFrequency(500);
        new downloadjni().start();
        new checkFinished().start();
    }
    /**
     * stop download test
     * */
    public void stop(){
    	state = Download.STATE_COMPLETE;
    	this.stopFromJNI();
    }

    class checkFinished extends Thread {
        public void run() {
            while (state == Download.STATE_RUNNING) {
                updateTraffic();
                try {
                    sleep(frequency);
                } catch (Exception e) {

                }
            }

        }
    };

    class downloadjni extends Thread {
        public void run() {
            startFromJNI();
            state = Download.STATE_COMPLETE;
            
            unoTest.getInfoUploadTrigger().setDownloadResult(getAveSpeed(), getMaxSpeed());
            unoTest.getInfoUploadTrigger().setServerOneResult(getServerOneAveSpeed());
            unoTest.getInfoUploadTrigger().setServerTwoResult(getServerTwoAveSpeed());
            unoTest.getInfoUploadTrigger().setMultiServerAddr(serverOneAddr, serverTwoAddr);
            
            new autoUploadThread().start();
            
            //Log.v("aaaa", ""+getPacketLossRatio());
        }
    }
    
    private class autoUploadThread extends Thread{
    	public void run(){
    		if(autoUpload){
            	PackData packData = new PackData();
            	JSONObject downloadjson = packData.packDownloadData(date.toString(), getAveSpeed(), getMaxSpeed(),unoTest.locationInfo.getBDLatitude(), unoTest.locationInfo.getBDLongitude(),serverAddress,unoTest.networkInfo.getNetworkType(), unoTest.hardwareInfo.getIMEI(), unoTest.networkInfo.getInternalIP(), unoTest.networkInfo.getExternalIP());
                Log.v("downloadtest",downloadjson.toString());
                String download_result_url = "http://xugang.host033.youdnser.com/serverPHP/updata_downloadSpeed_db.php";
                UploadData upload_download_result = new UploadData(download_result_url, downloadjson);
                String re = upload_download_result.upData();
                Log.v("downloadtest",re.toString());
            }
    	}
    }

    /** get type of current network, using class NetInfo */
    public String getNetworkType() {
        // TODO
        return null;
    }

    /** get the number of packages during this test */

    public int getPackage() {
        return -1;
    }

    /** get the number of lost packages during the test */
    public int getLostPackage() {
        return -1;
    }
    
    public String getTimeOfStart() {
    	return date;
    }

    /** set whether to upload the test data to our server */
    public boolean setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
        return true;
    }

    /** get state of the test */
    public int getState() {
        if (this.getTestState() == 0) {
            state = Download.STATE_RUNNING;
        } else {
            state = Download.STATE_COMPLETE;
        }
        return state;
    }
    
    public double getPacketLossRatio(){
    	DecimalFormat digit=new DecimalFormat("0.000");//取一位，如要取多位，写多几个0上去     	
    	return Double.valueOf(digit.format(getDropRatio()));
    }
}
