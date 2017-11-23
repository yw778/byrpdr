package com.example.sensortest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.docomo.Data.FileExport;
import com.docomo.Data.MyDatabase;
import com.example.sensortest.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends Activity {
	
	    String TAG = "HistoryActivity";
	    private ArrayList<Map<String, Object>> todoItems;
	    SimpleAdapter adapter;
	    DecimalFormat format = new DecimalFormat("0.00");
	    /*
	     * database
	     */
	    ListView myListView;
	    MyDatabase mydatabase;
	    Cursor mycursor;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.historypanel);
        this.mydatabase = new MyDatabase(this);
        
        final LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout lin = (LinearLayout) findViewById(R.id.historytitlelayout);
        findViewById(R.id.main);
        findViewById(R.id.backone);
        findViewById(R.id.midrelaLayout);      
        
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.historypanelserver, null).findViewById(
                R.id.aboutpanelLayout);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        Log.d("remoview", "remove view 1");
        lin.removeAllViews();
        try {
            lin.addView(layout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setList();
	}

    public void setList() {
        Log.d(TAG, "setList");
        myListView = (ListView) this.findViewById(R.id.history_list_view);
        if (mycursor != null) {
            mycursor.close();
        }
        todoItems = new ArrayList<Map<String, Object>>();
        mydatabase.open();
        mycursor = mydatabase.fetchDetailAllData();
        // id as
        // _id","time_index","upload_average","upload_max","download_average","download_max","latency","upload_traffic","download_traffic","network","providersname","ipInformation","testmode","lat","lon"
        if (mycursor == null) {
            Toast.makeText(this, "没有网速测试数据", Toast.LENGTH_SHORT).show();
            Log.v("mycursor is null", " mycursor is null");
        }
        if (mycursor.getCount() < 1) {
            Log.v("cursor", "getcount <0 ");
            mydatabase.close();
            return;
        }
        for (int i = 0; i < mycursor.getCount(); i++) {
            Log.v("mycursor", "mycursor i" + i);
            Map<String, Object> map = new HashMap<String, Object>();
            mycursor.moveToPosition(i);

            Date date = new Date(mycursor.getLong(1));
            int net;
            if (mycursor.getString(9).equals("Wi-Fi")) {
                net = R.drawable.wifi_icon;
            } else if (mycursor.getString(9).equals("2G")) {
                net = R.drawable.icon_2g;
            } else if (mycursor.getString(9).equals("3G")) {
                net = R.drawable.icon_3g;
            }

            else {
                net = R.drawable.icon_4g;
            }
            map.put("network", net);
            map.put("time_date", date.getYear() + 1900 + "/" + (date.getMonth() + 1) + "/" + date.getDate());
            String hours = "" + date.getHours();
            String minutes = "" + date.getMinutes();
            String seconds = "" + date.getSeconds();
            if (date.getHours() < 10) {
                hours = "0" + date.getHours();
            }
            if (date.getMinutes() < 10) {
                minutes = "0" + date.getMinutes();
            }
            if (date.getSeconds() < 10) {
                seconds = "0" + date.getSeconds();
            }
            String operator = getOperatorName(mycursor.getString(10));
            map.put("time_minute", hours + ":" + minutes + ":" + seconds);
            map.put("upload", String.valueOf(format.format((float) (Math.round(mycursor.getInt(2) * 1024 * 8 / 1048576f
                    * 100)) / 100)));
            map.put("upload_max", String.valueOf(format.format((float) (Math.round(mycursor.getInt(3) * 1024 * 8
                    / 1048576f * 100)) / 100)));
            map.put("download", String.valueOf(format.format((float) (Math.round(mycursor.getInt(4) * 1024 * 8
                    / 1048576f * 100)) / 100)));
            map.put("download_max", String.valueOf(format.format((float) (Math.round(mycursor.getInt(5) * 1024 * 8
                    / 1048576f * 100)) / 100)));
            map.put("ping", mycursor.getInt(6) + "");
            
            map.put("upload_traffic", mycursor.getDouble(7));
            map.put("download_traffic", mycursor.getDouble(8));
            
            map.put("providersname", operator);
            map.put("ipInformation", mycursor.getString(11));
            map.put("lat", mycursor.getString(13));
            map.put("lon", mycursor.getString(14));

            todoItems.add(map);

            // {"id as _id","time_index","upload_average","download_average","latency","network"}
        }
        Log.d("setList", "setList adapter");
        adapter = new SimpleAdapter(this, todoItems, R.layout.historylistitems, new String[] { "network", "time_date",
                "time_minute", "upload", "download", "ping" }, new int[] { R.id.textView_network,
                R.id.textView_time_date, R.id.textView_time_minute, R.id.textView_upload, R.id.textView_download,
                R.id.textView_ping });
        myListView.setAdapter(adapter);
        mycursor.close();
        mydatabase.close();

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d("historyActivity", "you press =" + arg2);
                Map<String, Object> map = (HashMap<String, Object>) myListView.getItemAtPosition(arg2);
                float upload = Float.valueOf(String.valueOf(map.get("upload")));
                float download = Float.valueOf(String.valueOf(map.get("download")));
                float upload_max = Float.valueOf(String.valueOf(map.get("upload_max")));
                float download_max = Float.valueOf(String.valueOf(map.get("download_max")));
                Log.d("historyActivity", "ping");
                String ping = (String) map.get("ping");
                String providersname = (String) map.get("ipInformation");
                int testmode = ((Integer) map.get("network")).intValue();
                String network;
                if (testmode == R.drawable.wifi_icon)
                    network = "Wi-Fi";
                else if (testmode == R.drawable.icon_2g)
                    network = "2G";
                else if (testmode == R.drawable.icon_3g)
                    network = "3G";
                else
                    network = "4G";
                String ipInformation = (String) map.get("ipInformation");
                Log.d("yylhistoryActivity",providersname + "\t" + ipInformation);
                String time = (String) map.get("time_date") + " " + (String) map.get("time_minute");
                String lat = (String) map.get("lat");
                String lon = (String) map.get("lon");
                double uploadtraffic = (Double) map.get("upload_traffic");
                double downloadtraffic = (Double)map.get("download_traffic");
                displayItemDetail(upload, upload_max, download, download_max, ping, uploadtraffic,downloadtraffic,providersname, network, testmode,
                        time, lat, lon, ipInformation);

            }
        });
	}
    public String getOperatorName(String operator) {
    		if (operator.equals("CMCC"))
	            return "中国移动";
	        else if (operator.equals("CUCC"))
	            return "中国联通";
	        else if (operator.equals("CTCC"))
	            return "中国电信";
	        else 
	        	return operator;
    }

    /**
     * 显示一条测试记录的详情
     * */
    public void displayItemDetail(float upload, float upload_max, float download, float download_max, String ping,double uploadtraffic,double downloadtraffic,
            String operator, String network, int testmode, String time, String lat, String lon, String ipInformation) {
        LayoutInflater inflater = LayoutInflater.from(this);
        // 引入窗口配置文件
        final View view = inflater.inflate(R.layout.historydetail, null);
        @SuppressWarnings("deprecation")
		final PopupWindow pop = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,true);
        // 设置点击窗口外边窗口消失
        pop.setOutsideTouchable(true);
        pop.showAsDropDown(view);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                pop.dismiss();
            }
        });

        TextView tv_download;
        TextView tv_upload;
        TextView tv_latency;
        TextView tv_time;
        TextView tv_networktype;
        TextView tv_operator;
        TextView tv_ip;
        TextView tv_location;
        TextView tv_uptraffic;
        
        
        tv_download = (TextView) view.findViewById(R.id.tv_download_result);
        tv_upload = (TextView) view.findViewById(R.id.tv_upload_result);
        tv_latency = (TextView) view.findViewById(R.id.tv_ping_result);
        tv_networktype = (TextView) view.findViewById(R.id.tv_networktype_result);
        tv_operator = (TextView) view.findViewById(R.id.tv_operator_result);
        tv_time = (TextView) view.findViewById(R.id.tv_time_result);
        tv_ip = (TextView) view.findViewById(R.id.tv_ip_result);
        tv_location = (TextView) view.findViewById(R.id.tv_location_result);
        tv_time = (TextView) view.findViewById(R.id.tv_time_result);
        tv_uptraffic = (TextView)view.findViewById(R.id.uptrafdetail);
        
        tv_download.setText(download + "/" + download_max + " Mbps");
        tv_upload.setText(upload + "/" + upload_max + " Mbps");
        tv_latency.setText(ping + " ms");
        tv_networktype.setText(network);
        tv_operator.setText(operator);
        tv_ip.setText(ipInformation);
        tv_time.setText(time);
        tv_location.setText(lat + "-" + lon);
        tv_uptraffic.setText(uploadtraffic+"MB"+"/"+downloadtraffic+"MB");
    }
    
}
