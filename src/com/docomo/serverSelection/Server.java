package com.docomo.serverSelection;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class Server {
    Context context;

    public Server(Context context) {
        this.context = context;

    }

    public List<ServerData> getTestServer() {

        List<ServerData> servers = new ArrayList<ServerData>(6);
        ServerData[] serverdata = new ServerData[6];
        serverdata[0] = new ServerData(0, "http://buptant.cn/UNOTest/speedtest", "北京市 万网IDC机房", "beijing",
                39.9139, 116.3917, "ccde", 5);
        serverdata[1] = new ServerData(1, "http://xugang.host033.youdnser.com/UNOTest/speedtest", "北京石景山", "beijing",
                39.9139, 116.3917, "China Unicom", 5);
        serverdata[2] = new ServerData(2, "http://down.yiinet.net/speedtest/speedtest", "北京西城区", "beijing", 39.9139,
                116.3917, "Capital Online Data Service", 5);
        serverdata[3] = new ServerData(3, "http://ccpn.3322.org/speedtest", "中国四川成都", "beijing", 30.6597, 104.0633,
                "China Unicom", 5);
        serverdata[4] = new ServerData(4, "http://buptantlab.com.am76.nb118.net/UNOTest/speedtest", "美国 加利福尼亚州",
                "beijing", 39.9139, 119.0633, "China Unicom", 5);
        serverdata[5] = new ServerData(5, "http://speed.dtgt.org/speedtest", "中国北京西城2", "beijing", 39.9139, 116.3917,
                "Beijing Normal University", 5);
        for (int i = 0; i < 6; i++) {
            servers.add(serverdata[i]);
        }

        return servers;

    }

}
