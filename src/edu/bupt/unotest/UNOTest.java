package edu.bupt.unotest;

import com.docomo.serverSelection.ServerInfo;

import edu.bupt.nettest.Download;
import edu.bupt.nettest.Latency;
import edu.bupt.nettest.ThirdDownload;
import edu.bupt.nettest.Upload;
import edu.bupt.testinfo.HardwareInfo;
import edu.bupt.testinfo.InfoUploadTrigger;
import edu.bupt.testinfo.LocationInfo;
import edu.bupt.testinfo.NetInfo;
import android.content.Context;

public class UNOTest {

    Context context;
    /**
     * 服务器信息
     */
    public ServerInfo serverInfo;
    // selectTestServer st;
    /**
     * 硬件信息
     */

    public HardwareInfo hardwareInfo;

    /**
     * 网络信息
     */
    public NetInfo networkInfo;
    /**
     * 地理位置信息
     */
    public LocationInfo locationInfo;

    /**
     * 测试信息
     */
    private Download download;
    private Upload upload;
    private Latency latency;
    private ThirdDownload thirddownload;

    /**
     * 信息上报控制
     */
    private InfoUploadTrigger infoUploadTrigger;

    /**
     * UNOTest
     * 
     * @param context
     */

    public UNOTest(Context context) {
        this.context = context;
        this.initLocationInfo(context);
        this.initHardwareInfo(context);
        this.initNetworkInfo(context);
        this.initServerInfo(this, context);
        this.initInfoUploadTrigger(context);
    }

    public void onDestory() {
//        this.locationInfo.onDetory();
        this.infoUploadTrigger.destroyInfoUploadTrigger();
        this.serverInfo.onDestory();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public Latency getLatencyTest() {
        latency = new Latency(this);
        return latency;
    }

    public Download getDownloadTest() {
        download = new Download(this);
        return download;
    }

    public Upload getUploadTest() {
        upload = new Upload(this);
        return upload;
    }

    public ThirdDownload getThirdDownloadTest() {
        thirddownload = new ThirdDownload(this);
        return thirddownload;
    }

    public InfoUploadTrigger getInfoUploadTrigger() {
        return infoUploadTrigger;
    }
//
//    // //////////////////////////////////////////////////////////////////////////////////////////////////
//
    private void initNetworkInfo(Context context) {
        networkInfo = new NetInfo(context, this);
    }
//
//    // ///////////////////////////////////////////////////////////////////////////////////////////////
    private void initHardwareInfo(Context context) {
        hardwareInfo = new HardwareInfo(context);
    }
//
//    // /////////////////////////////////////////////////////////////////////////////////////////////
    private void initLocationInfo(Context context) {
        locationInfo = new LocationInfo(context);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////
    private void initServerInfo(UNOTest unoTest, Context context) {
        serverInfo = new ServerInfo(unoTest, context);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    private void initInfoUploadTrigger(Context context) {
        infoUploadTrigger = new InfoUploadTrigger(context, this);
    }
}
