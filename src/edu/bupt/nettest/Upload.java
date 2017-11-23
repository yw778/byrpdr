package edu.bupt.nettest;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import edu.bupt.testinfo.PackData;
import edu.bupt.testinfo.UploadData;
//import edu.bupt.testinfo.PackData;
//import edu.bupt.testinfo.UploadData;
import edu.bupt.unotest.UNOTest;
import android.util.Log;

/** Part of test API, upload test */

public class Upload {
    /** final variables */
    // Created by x7, Mar 14, 2013
    public static final int STATE_READY = 0x0; // states of test
    public static final int STATE_STARTED = 0x1;
    public static final int STATE_RUNNING = 0x2; // Modified by x7, Mar 15, 2013
    public static final int STATE_COMPLETE = 0x3;
    public static final int STATE_KILLED = 0x4;
    private final String DEBUGTAG = "zzz.debug.upload";
    public static final int NETWORK_MOBILE = 0x0;
    public static final int NETWORK_WIFI = 0x1;

    /** Variables, not described in the doc */
    // Created by x7, Mar 14, 2013
    private int testTime = 10; // time of one test, in 's'
    // AssetManager assetManager = null;
    private String localFileLocation = null;
    // private int threadnumber = 3; // number of uploading threads
    // private int bufferSize = 1024 * 8;
    // private int testway = 0; // method of test, http or socket
    // (not available in 1st version)
    // private int currentSpeed = 0; // current speed
    private int infoFrequency = 100000; // frequency of data updating, us
    // private int averageSpeed = 0; // average speed
    // private int maxSpeed = 0; // max speed
    // private int packages = 0; // packages during the test
    // (not available in 1st version)
    // private int lostPackages = 0; // lost packages during the test
    // (not available in 1st version)
    private boolean autoUpload = true; // whether to upload the test data to our
                                       // server
    private int state = Upload.STATE_READY; // state of the test
    private long timeOfStart;
    private uploadControllerThread controller;

    private UNOTest unoTest;

    private String date;

    private String serverAddress0 = "buptant.cn/UNOTest/speedtest/upload.php";
    private String serverAddress1 = "";
    // private String serverAddress2;
    private String serveraddrUpload;

    private native double getDropRatio();

    public Upload(UNOTest unoTest) {
        this.unoTest = unoTest;
    }

    /** Functions, as the doc described */
    /** set the value of testTime, in 'ms' */
    // Created by x7, Mar 14, 2013
    public boolean setTestTime(int testTime) {
        this.testTime = testTime;
        return true;
    }

    /** set a list as testServer */
    // Created by x7, Mar 14, 2013
    // Modified by x7, Jun 24, 2013
    public boolean setTestServer(String s0) {
        this.serverAddress0 = s0;
        this.serverAddress1 = "";
        this.serveraddrUpload = s0;
        return true;
    }

    // for multi-server test
    // Modified by x7, Jun 24, 2013
    public boolean setTestServer(String s0, String s1) {
        this.serverAddress0 = s0;
        this.serverAddress1 = s1;
        this.serveraddrUpload = "multi-server";
        // this.serverAddress2 = serverAddress2;
        // this.setPthreadNum(3);
        return true;
    }

    /** set network type, use big file to upload in wifi network */
    // Created by x7, Jun 20, 2013
    public boolean setNetworkType(int t) {
        csetNetworkType(t);
        return true;
    }

    /** set upload pthread number */
    // Created by x7, Jun 20, 2013
    public boolean setPthreadNum(int n) {
        csetPthreadNum(n);
        return true;
    }

    /** set local file location */
    // Created by x7, Mar 14, 2013
    // public boolean setLocalFileLocation(String fileLocatoin) {
    // this.localFileLocation = fileLocatoin;
    // return true;
    // }

    // Created by x7, Mar 14, 2013
    // public boolean setLocalFileLocation(AssetManager assetManager) {
    // this.assetManager = assetManager;
    // return true;
    // }

    /** set number of uploading threads */
    // Created by x7, Mar 14, 2013
    // public boolean setThreadNumber(int threadnumber) {
    // this.threadnumber = threadnumber;
    // return true;
    // }

    /** set buffer size of uploading */
    // Created by x7, Mar 14, 2013
    // public boolean setBufferSize(int size) {
    // bufferSize = size;
    // return true;
    // }

    /** set method of test, http or socket */
    // (not available in 1st version)
    // Created by x7, Mar 14, 2013
    // public boolean setTestWay(int testway) {
    // this.testway = testway;
    // return true;
    // }

    /** set frequency of updating */
    // Created by x7, Mar 14, 2013
    public boolean setInfoFrequency(int time) {
        this.infoFrequency = time;
        return true;
    }

    /** start this test */
    // Created by x7, Mar 14, 2013
    public boolean start() {
        if (state == Upload.STATE_READY) {
            state = Upload.STATE_RUNNING; // set state
                                          // Modified by x7, Mar 15, 2013
            Log.d(DEBUGTAG, "state == " + state);
            // timeOfStart = System.currentTimeMillis();
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            date = formatter.format(curDate);
            new uploadControllerThread().start();
            new checkFinished().start();
            return true;
        }
        return false;
    }
    public void stop(){
    	this.forcestop();
    	state=Download.STATE_COMPLETE;
    }

    class checkFinished extends Thread {
        public void run() {
            while (state == Download.STATE_RUNNING) {
                refreshTraffic();
                try {
                    sleep(infoFrequency / 1000);
                } catch (Exception e) {

                }
            }

        }
    };

    /** kill this test, for some unforeseen reasons */
    // Created by x7, Mar 14, 2013
    public boolean kill() {
        controller.kill();
        return false;
    }

    /** get time of the test */
    // Created by x7, Mar 14, 2013
    public long getTime() {
        return controller.getActualTestTime();
    }

    /** get type of current network, using class NetInfo */
    // Created by x7, Mar 14, 2013
    public String getNetworkType() {
        // TODO
        return null;
    }

    /** get current speed */
    // Created by x7, Mar 14, 2013
    public int getCurrentSpeed() {
        if (state == Upload.STATE_RUNNING) {
            return cgetCurrentSpeed();
        }
        return -1;
    }

    /** get average speed */
    // Created by x7, Mar 14, 2013
    public int getAverageSpeed() {
        if (state == Upload.STATE_COMPLETE || state == Upload.STATE_KILLED) {
            return cgetAverageSpeed();
        }
        return -1;
    }

    /** get max speed */
    // Created by x7, Mar 14, 2013
    public int getMaxSpeed() {
        if (state == Upload.STATE_COMPLETE || state == Upload.STATE_KILLED) {
            return cgetMaxSpeed();
        }
        return -1;
    }

    /** get time stramp of the start of test */
    // Created by x7, Mar 14, 2013
    public String getTimeOfStart() {
        return date;
    }

    /** get the number of packages during this test */
    // (not available in 1st version)
    // Created by x7, Mar 14, 2013
    public int getPackage() {
        return -1;
    }

    /** get the number of lost packages during the test */
    // (not available in 1st version)
    // Created by x7, Mar 14, 2013
    public int getLostPackage() {
        return -1;
    }

    /** set whether to upload the test data to our server */
    // Created by x7, Mar 14, 2013
    public boolean setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
        return true;
    }

    /** get state of the test */
    // Created by x7, Mar 14, 2013
    public int getState() {
        return state;
    }

    /** thread for controlling uploading */
    // Created by x7, Mar 27, 2013
    private class uploadControllerThread extends Thread {

        // private String loc =
        // private boolean forceKillFlag = false;
        public void run() {
            state = Upload.STATE_RUNNING; // change state
            // ctestUpload(serverAddress0, testTime, infoFrequency);
            // Log.v("serverAddress", "" + serverAddress0);

            if (serverAddress1 == "") {
                Log.i("zzz", "******* single server *******");
                ctestUpload(serverAddress0, testTime, infoFrequency);
            } else {
                Log.i("zzz", "******* multi server *******");
                Log.i("zzz", "*******" + testTime);
                ctestUploadMultiServer(serverAddress0, serverAddress1,
                        testTime, infoFrequency);
            }
            state = Upload.STATE_COMPLETE; // change state

            unoTest.getInfoUploadTrigger().setUploadResult(getAverageSpeed(),
                    getMaxSpeed());

            new autoUploadThread().start();

        }

        public long getActualTestTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        public void kill() {
            // TODO Auto-generated method stub

        }
    }

    private class autoUploadThread extends Thread {
        public void run() {
            if (autoUpload) {
                // Log.v(TAG,uploadjson.toString());

                PackData packData = new PackData();
                JSONObject uploadjson = packData.packUploadData(
                        date.toString(), getAverageSpeed(), getMaxSpeed(),
                        unoTest.locationInfo.getBDLatitude(),
                        unoTest.locationInfo.getBDLongitude(),
                        serveraddrUpload, unoTest.networkInfo.getNetworkType(),
                        unoTest.hardwareInfo.getIMEI(), ""
                                + unoTest.networkInfo.getInternalIP(), ""
                                + unoTest.networkInfo.getExternalIP());
                Log.v("uploadtest", uploadjson.toString());
                String upload_result_url = "http://xugang.host033.youdnser.com/serverPHP/updata_uploadSpeed_db.php";
                UploadData upload_upload_result = new UploadData(
                        upload_result_url, uploadjson);
                String re = upload_upload_result.upData();
                Log.v("uploadtest", re.toString());
            }
        }
    }

    /** start uploading, in multi threads */
    // Created by x7, Mar 27, 2013
    private native int ctestUpload(String testUrl, int timeout_s,
            int interval_us);

    /** Multi Server */
    // by x7, May17, 2013
    // Modified by x7, Jun 24, 2013
    private native int ctestUploadMultiServer(String serverAddress0,
            String serverAddress1, int timeout_s, int interval_us);

    /** get data from C func */
    // Created by x7, Mar 29, 2013
    private native int cgetMaxSpeed();

    private native int cgetAverageSpeed();

    private native int cgetCurrentSpeed();

    private native int csetPthreadNum(int n);

    private native int csetNetworkType(int t);

    public native int refreshTraffic();
    
    public native int forcestop();

    private native void csetTrafficDuration(int uploadtraffic);
    public void setUploadTraffic(int uploadtraffic){
    	csetTrafficDuration(uploadtraffic);
    }
    
    static {
        System.loadLibrary("nettest");
    }

    public double getPacketLossRatio() {
        DecimalFormat digit = new DecimalFormat("0.000");// 閸欐牔绔存担宥忕礉婵″倽顪呴崣鏍ь檵娴ｅ稄绱濋崘娆忣檵閸戠姳閲�娑撳﹤骞�
        return Double.valueOf(digit.format(getDropRatio()));
    }

    /** thread for controlling uploading */
    // Created by x7, Mar 14, 2013
    // private class uploadControllerThread extends Thread {
    // private String speedtestURL = testServer.get(0)
    // + "/UNOTest/upload/upload_file.php";
    // private long beginTime;
    // private long currentTime;
    // private long lastTime = 0;
    // private long actualTestTime = 0;
    // private long beginTrafficTotal = 0;
    // private long lastTrafficTotal = 0;
    // private long currentTrafficTotal = 0;
    //
    // private boolean forceKill = false;
    //
    // public void run() {
    // Log.d(DEBUGTAG, "upload controller started");
    // String remoteURL = speedtestURL;
    // uploadThread u = new uploadThread(assetManager, remoteURL);
    // u.run();
    //
    // // while (!u.initialComplete) { // flag for debug
    // // try {
    // // sleep(100);
    // // Log.d(DEBUGTAG, "waiting for initialiazition");
    // // } catch (InterruptedException e) {
    // // e.printStackTrace();
    // // }
    // // }
    // beginTime = System.currentTimeMillis();
    // beginTrafficTotal = TrafficStats.getTotalTxBytes();
    // lastTime = beginTime;
    // lastTrafficTotal = beginTrafficTotal;
    //
    // while (u.isFinished() == false && forceKill == false) {
    // try {
    // sleep(infoFrequency);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // currentTime = System.currentTimeMillis();
    // currentTrafficTotal = TrafficStats.getTotalTxBytes();
    // if ((currentTime - beginTime) < testTime && !u.isFinished()) {
    // // currentTime = System.currentTimeMillis();
    // // currentTrafficTotal = mTrafficStats.getTotalRxBytes();
    // currentSpeed = (int) ((currentTrafficTotal - lastTrafficTotal) /
    // (currentTime - lastTime));
    //
    // state = STATE_RUNNING; // change state
    // // modified by x7, Mar 15, 2013
    // Log.d(DEBUGTAG, "state == " + state);
    //
    // Log.d(DEBUGTAG, "currentSpeed == " + currentSpeed);
    // lastTime = currentTime;
    // lastTrafficTotal = currentTrafficTotal;
    // if (maxSpeed < currentSpeed) {
    // maxSpeed = currentSpeed;
    // }
    // } else {
    // u.kill();
    //
    // Log.d(DEBUGTAG, "kill");
    // break;
    // }
    // }
    // if (forceKill) {
    // state = Download.STATE_KILLED;
    // Log.d(DEBUGTAG, "state == " + state);
    // } else {
    // state = Download.STATE_COMPLETE;
    // Log.d(DEBUGTAG, "state == " + state);
    // }
    // actualTestTime = currentTime - beginTime;
    // averageSpeed = (int) ((currentTrafficTotal - beginTrafficTotal) /
    // (currentTime - beginTime));
    // Log.d(DEBUGTAG, "averageSpeed == " + averageSpeed);
    // }
    //
    // public long getActualTestTime() {
    // return actualTestTime;
    // }
    //
    // public void kill() {
    // forceKill = true;
    // }
    // }

    /** thread for uploading */
    // Created by x7, Mar 14, 2013
    // private class uploadThread extends Thread {
    // private AssetManager assetManager;
    // // private String Name;
    // private String remoteURL;
    // // private boolean initialComplete = false;
    // private boolean allThreadsFinished = false;
    //
    // SingleFileUploadThread mSingleFileUploadThread[];
    //
    // public uploadThread(AssetManager assetManager, String remoteURL) {
    // this.assetManager = assetManager;
    // // this.Name=Name;
    // this.remoteURL = remoteURL;
    // }
    //
    // @Override
    // public void run() {
    // Log.d(DEBUGTAG, "upload starting");
    // mSingleFileUploadThread = new SingleFileUploadThread[threadnumber];
    //
    // for (int i = 0; i < threadnumber; i++) {
    // mSingleFileUploadThread[i] = new SingleFileUploadThread();
    // mSingleFileUploadThread[i].start();
    // }
    // // initialComplete = true;
    // }
    //
    // public boolean isFinished() {
    // allThreadsFinished = false;
    // for (int i = 0; i < threadnumber; i++) {
    // if (mSingleFileUploadThread[i].isFinished() == true) {
    // allThreadsFinished = true;
    // this.kill();
    // Log.d(DEBUGTAG, "download killed");
    // break;
    // }
    // }
    // return allThreadsFinished;
    // }
    //
    // /** force stop the other threads */
    // public boolean kill() {
    // allThreadsFinished = true;
    // for (int i = 0; i < threadnumber; i++) {
    // mSingleFileUploadThread[i].kill();
    // }
    // return true;
    // }
    //
    // private class SingleFileUploadThread extends Thread {
    // private int timeout = testTime; // set in the outer class
    // // private URL url;
    // private File file;
    // private String newName = "upload-test.jpg";
    // byte[] buffer;
    // private InputStream fis;
    // // private int startPosition;
    // // private int endPosition;
    // // private int curPosition;
    // private boolean singleThreadFinished = false; // whether this thread
    // // has finished
    // private boolean forceKill = false;
    //
    // @Override
    // public void run() {
    // String end = "\r\n";
    // String twoHyphens = "--";
    // String boundary = "******";
    // try {
    // // long begintime = System.currentTimeMillis();
    // Log.d(DEBUGTAG, "upload thread starting");
    // URL url = new URL(remoteURL);
    // Log.d(DEBUGTAG, "URL == " + remoteURL);
    // HttpURLConnection httpURLConnection = (HttpURLConnection) url
    // .openConnection();
    // httpURLConnection.setConnectTimeout(timeout);
    // // 鐠佸墽鐤嗗В蹇旑偧娴肩姾绶惃鍕ウ婢堆冪毈閿涘苯褰叉禒銉︽箒閺佸牓妲诲銏″閺堝搫娲滄稉鍝勫敶鐎涙ü绗夌搾鍐茬┛濠э拷 // //
    // 濮濄倖鏌熷▔鏇犳暏娴滃骸婀０鍕帥娑撳秶鐓￠柆鎾冲敶鐎瑰綊鏆辨惔锔芥閸氼垳鏁ゅ▽鈩冩箒鏉╂稖顢戦崘鍛村劥缂傛挸鍟块惃锟紿TTP
    // 鐠囬攱鐪板锝嗘瀮閻ㄥ嫭绁﹂妴锟� // httpURLConnection.setChunkedStreamingMode(64 *
    // 1024);// 64K
    // // 閸忎浇顔忔潏鎾冲弳鏉堟挸鍤ù锟� // httpURLConnection.setDoInput(true);
    // httpURLConnection.setDoOutput(true);
    // httpURLConnection.setUseCaches(false);
    // // 娴ｈ法鏁OST閺傝纭� // httpURLConnection.setRequestMethod("POST");
    // httpURLConnection.setRequestProperty("Connection",
    // "Keep-Alive");
    // httpURLConnection.setRequestProperty("Charset", "UTF-8");
    // httpURLConnection.setRequestProperty("Content-Type",
    // "multipart/form-data;boundary=" + boundary);
    //
    // DataOutputStream dos = new DataOutputStream(
    // httpURLConnection.getOutputStream());
    // dos.writeBytes(twoHyphens + boundary + end);
    // dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
    // + newName.substring(newName.lastIndexOf("/") + 1)
    // + "\"" + end);
    // dos.writeBytes(end);
    //
    // if (localFileLocation != null) {
    // fis = new FileInputStream(file);
    // } else {
    // fis = assetManager.open("upload.jpg");
    // Log.d(DEBUGTAG, "upload.jpg  " + fis);
    // }
    // buffer = new byte[8192]; // 8k
    // int count = 0;
    // // 鐠囪褰囬弬鍥︽
    //
    // while ((count = fis.read(buffer)) != -1 && !forceKill) {
    // dos.write(buffer, 0, count);
    // // Log.d(DEBUGTAG, "0");
    // }
    // fis.close();
    // dos.writeBytes(end);
    // dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
    // dos.flush();
    //
    // int res = httpURLConnection.getResponseCode();
    // if (res == 200) {
    // // long endtime = System.currentTimeMillis();
    // // uploadtime = endtime - begintime;
    // // uploadspeed = (int) (uploadsize / uploadtime);
    // dos.close();
    // httpURLConnection.disconnect();
    // }
    // Log.d(DEBUGTAG, "upload thread stopped, res == " + res);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    //
    // public boolean isFinished() {
    // return singleThreadFinished;
    // }
    //
    // public boolean kill() {
    // forceKill = true;
    // return true;
    // }
    // }
    // }
}
