package com.docomo.serverSelection;

import java.util.List;
import java.util.Random;

import edu.bupt.unotest.UNOTest;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ServerInfo {

    private UNOTest unoTest;
    private Context context;
    private boolean isAlive = false;
    private String TAG = "ServerInfo";
    private boolean isAutoSelected = true;

    public String[] testServer = new String[3];
    private String nameOfLatency = "/latency.txt";
    private String nameOfMobileDownload = "/random350x350.jpg";
    private String nameOfMobileUpload = "/upload.php";
    private String nameOfWifiDownload = "/random1500x1500.jpg";
    private String nameOfWifiUpload = "/upload.php";

    private static List<ServerData> testServerData; // 存储服务器地址对象json格式
    private static int serverID = 0;

    public ServerInfo(UNOTest unoTest, Context context) {// 构造函数
        isAlive = true;
        this.unoTest = unoTest;
        this.context = context;
        testServer[0] = "http://buptant.cn/UNOTest/speedtest";
        testServer[1] = "http://speed.dtgt.org/speedtest";
        testServer[2] = "http://xugang.host033.youdnser.com/UNOTest/speedtest";

        new getLocalServerListThread().run();
        startAutoServerSelection();
    }

    public boolean startAutoServerSelection() {
        new ServerSelect().start();
        return true;
    }

    public void onDestory() {
        isAlive = false;
    }

    /**
     * 
     * 该类需要设置状态标记点，指示出是否服务器获取正常
     */
    // 获取server
    private class getLocalServerListThread extends Thread {
        public void run() {
            Server servers = new Server(context);
            testServerData = servers.getTestServer();
            Log.v(TAG, "本地列表获取成功");
        }
    }

    public boolean getServerState() {
        if (testServerData != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getAutoSelectState() {
        return isAutoSelected;
    }

    public void setAutoSelection() {
        isAutoSelected = true;

    }

    public void setManualSelection() {
        isAutoSelected = false;

    }

    public String getTwoMobileLatencyTestServerAddress() {
        if (testServerData != null) {
            if (serverID != 0) {
                return testServer[1] + nameOfLatency;
            } else {
                return testServer[2] + nameOfLatency;
            }
        } else {
            return testServer[0] + nameOfLatency;
        }
    }

    public String getOneMobileLatencyTestServerAddress() {
        if (testServerData != null) {
            return testServer[0] + nameOfLatency;
        } else {
            return testServer[2] + nameOfLatency;
        }
    }

    public String getTwoMobileDownloadTestServerAddress() {
        if (testServerData != null) {
            if (serverID != 0) {
                return testServer[1] + nameOfMobileDownload;
            } else {

                return testServer[2] + nameOfMobileDownload;
            }

        } else {
            return testServer[0] + nameOfMobileDownload;
        }
    }

    public String getOneMobileDownloadTestServerAddress() {
        if (testServerData != null) {
            return testServer[0] + nameOfMobileDownload;
        } else {
            return testServer[2] + nameOfMobileDownload;
        }
    }

    public String getTwoMobileUploadTestServerAddress() {
        if (testServerData != null) {
            if (serverID != 0) {
                return testServer[1] + nameOfMobileUpload;
            } else {
                return testServer[2] + nameOfMobileUpload;
            }
        } else {
            return testServer[0] + nameOfMobileUpload;
        }
    }

    public String getOneMobileUploadTestServerAddress() {
        if (testServerData != null) {
            return testServer[0] + nameOfMobileUpload;
        } else {
            return testServer[2] + nameOfMobileUpload;
        }
    }

    public String getTwoWifiLatencyTestServerAddress() {
        if (testServerData != null) {
            if (serverID != 0) {
                return testServer[1] + nameOfLatency;
            } else {
                return testServer[2] + nameOfLatency;

            }
        } else {
            return testServer[0] + nameOfLatency;
        }
    }

    public String getOneWifiLatencyTestServerAddress() {
        if (testServerData != null) {
            return testServer[0] + nameOfLatency;
        } else {
            return testServer[2] + nameOfLatency;
        }
    }

    public String getTwoWifiDownloadTestServerAddress() {
        if (testServerData != null) {
            if (serverID != 0) {
                return testServer[1] + nameOfWifiDownload;
            } else {
                return testServer[2] + nameOfWifiDownload;

            }
        } else {
            return testServer[0] + nameOfWifiDownload;
        }
    }

    public String getOneWifiDownloadTestServerAddress() {
        if (testServerData != null) {
            return testServer[0] + nameOfWifiDownload;
        } else {
            return testServer[1] + nameOfWifiDownload;
        }
    }

    public String getTwoWifiUploadTestServerAddress() {
        if (testServerData != null) {
            if (serverID != 0) {
                return testServer[1] + nameOfWifiUpload;
            } else {

                return testServer[2] + nameOfWifiUpload;
            }
        } else {
            return testServer[0] + nameOfWifiUpload;
        }
    }

    public String getOneWifiUploadTestServerAddress() {
        if (testServerData != null) {
            return testServer[0] + nameOfWifiUpload;
        } else {
            return testServer[2] + nameOfWifiUpload;
        }
    }

    public int getTestServerId() {
        return serverID;
        // return 4;
    }

    public String getTestServers() {
        return testServer[0] + "||" + testServer[1];
    }

    public String getTestServerName() {
        if (testServerData != null) {
            return testServerData.get(serverID).getServerName();
        }
        return testServerData.get(0).getServerName();

    }

    public List<ServerData> getServerData() {
        return testServerData;
    }

    public boolean setServerId(int serverId) {
        serverID = serverId;
        testServer[0] = testServerData.get(serverID).getUrl();
        testServer[1] = testServer[0];
        return true;
    }

    /**
     * 服务器选择
     * */
    private class ServerSelect extends Thread {
        @Override
        public void run() {
            // 程序已销毁
            if (!isAlive)
                return;
            Log.w(TAG, TAG + " isAutoSelected=" + isAutoSelected);
            if (!isAutoSelected)
                return;
            serverSelect s = new serverSelect();
//            s.setlat(unoTest.locationInfo.getBDLatitude());
//            s.setlon(unoTest.locationInfo.getBDLongitude());
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            if (unoTest.networkInfo.getNetworkType().equals("Wi-Fi")) {
//                s.setnetwork_type("w");
//                s.setnetwork_operator(unoTest.networkInfo.getExternalIP());// plmn
//                s.setnetwork_standard(unoTest.networkInfo.getTheIpinfo());//
//            } else {
//                // 这部分没问题
//                s.setnetwork_type("m");
//                s.setnetwork_operator(tm.getNetworkOperator().toString());// plmn
//                s.setnetwork_standard(unoTest.networkInfo.getNetworkStandard() + "");
//            }
            s.start();
            int wait = 30;
            while (!s.isEnd && wait > 0) {
                wait--;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            testServer[0] = s.firstURL;
            testServer[1] = s.secondURL;
            testServer[2] = s.thirdURL;
            Random random = new Random();
            serverID = random.nextInt(3);
            Log.v(TAG, TAG + " firstURL: " + testServer[0]);
            Log.v(TAG, TAG + " secondURL: " + testServer[1]);
            Log.v(TAG, TAG + " thirdURL: " + testServer[2]);

        }
    };
}
