package edu.bupt.anttest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sensortest.GraphActivity;
import com.docomo.Data.FileExport;
import com.docomo.Data.TestStatus;
import com.docomo.Data.MyDatabase;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
//import edu.bupt.anttest.push.PushMsgBroadcastReceiver;
import edu.bupt.nettest.*;
import edu.bupt.testinfo.UploadData;
//import edu.bupt.testinfo.UploadData;
import edu.bupt.unotest.UNOTest;

public class MainService extends Service {
	public static long testtimes = 0;
	/**
	 * Activity和Service通信类声明
	 */
	public static final String Service_Data = "edu.bupt.anttest";
	public static final String TAG = "mainservice";
	private final IBinder mBinder = new LocalBinder();
	Intent intent_communication = new Intent(Service_Data);

	/**
	 * 
	 * 数据库类存储
	 * */
	MyDatabase mydatabase;
	public double lat;
	public double lon;
	public int floor;
	/**
	 * 
	 * 数据库类游标
	 * */
	Cursor mycursor;

	/**
	 * 监测运营网络声明
	 */
	private Handler mainHandler = new Handler();
	Boolean networkStatus = false; // 网络连通状态
	int link_type = 0; // 网络连接类型 mobile:0 wifi:1
	public String networkType = "正在获取";
	public String providersname = "正在获取";
	public String ipInformation = "正在获取";

	/**
	 * 测试信息声明
	 */
	// AssetManager assetManager;
	public int currentSpeed = 0;
	public Download d;
	public Upload u;
	public ThirdDownload thirddownload;
	public Boolean disrunning = false;
	public Boolean uisrunning = false;
	public Boolean isAbort = true; // 是否暂停

	public int ave_download_speed = 0;
	public int max_download_speed = 0;
	public int ave_upload_speed = 0;
	public int max_upload_speed = 0;
	public int ping_latency = 0;
	public long timeOfStart;

	/**
	 * 测试流程控制属性
	 * */
	public int test_controller_status = -1;

	public Context mContext;

	static UNOTest unoTest;

	public static int testModel = 0;// 0为服务器测试，1为网站测试
	public static String weburl = "www.sina.com.cn";
	public static String webname = "";
	public static int webID = 0;

	public static String downloadrank;
	public static String uploadrank;

	private int uptrafficduration;
	private int downtrafficduration;
	private double upload_traffic;
	private double download_traffic;
	/*
	 * 测试一次统计使用的流量
	 */
	public static long uploadtrafficstart = 0;
	public static double uploadtrafficusage = 0;
	public static long downloadtrafficstart = 0;
	public static double downloadtrafficusage = 0;

	private String mobileOneLatencyServerAddress,
			mobileTwoLatencyServerAddress;
	private String wifiOneDownloadServerAddress, wifiTwoDownloadServerAddress;
	private String mobileOneDownloadServerAddress,
			mobileTwoDownloadServerAddress;
	private String mobileOneUploadServerAddress, mobileTwoUploadServerAddress;
	
	private String FILE_PATH = null;
	private String fileName = "";

	public void onCreate() {
		super.onCreate();

		unoTest = new UNOTest(this);

		Log.v("SERVICE", "onCreate");
		// 获取网络状态的先不加zj
		new networkListen().start();// /开启网络监控类

		/**
		 * 数据库初始化
		 * */
		mydatabase = new MyDatabase(this);// /数据库初始化
		intent_communication.putExtra("ServiceState", "currentSpeedUpdate");
		/**
		 * 第一次开启程序，程序是第一次启动，改变状态变量
		 * */
		TestStatus.testingStatus = TestStatus.FirstBoot;
		
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " "  
                + "hh:mm:ss");   
		fileName =  tempDate.format(new java.util.Date()).toString() + "indoorSpeedPDR";


	}

	/***
	 * refreshTestData用于清空数据，当用户进行服务器设置的时候需要清空数据
	 * */
	public void refreshTestData() {
		ave_download_speed = 22;
		max_download_speed = 22;
		ave_upload_speed = 22;
		max_upload_speed = 22;
		ping_latency = 22;

	}

	public void onStart(Intent intent, int startId) {
		Log.v("SERVICE", "onStart");
		super.onStart(intent, startId);
		Log.d(TAG, "onstart is running");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		// check whether is a push test, zzz
		// if (PushMsgBroadcastReceiver.TESTPUSH_ACTION != null && intent !=
		// null && intent.hasExtra(PushMsgBroadcastReceiver.TESTPUSH_ACTION)
		// && intent.getBooleanExtra(PushMsgBroadcastReceiver.TESTPUSH_ACTION,
		// false)) {
		// // start test
		// Log.d("SERVICE", "push test start");
		// ConnectivityManager();
		// startTest();
		// }
		ConnectivityManager();
		startTest();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v("SERVICE", "onBind");
		return mBinder;
	}

	public void onDestroy() {
		Log.v("SERVICE", "onDestroy");
		unoTest.onDestory();
		super.onDestroy();
	}

	public class LocalBinder extends Binder {
		public MainService getService() {
			// 返回Activity所关联的Service对象，这样在Activity里，就可调用Service里的一些公用方法和公用属性
			return MainService.this;
		}
	}

	/**
	 * 主要测试任务
	 * 
	 */

	public void setLimition(int limition) { // limition单位是KB，要换算成MB
		Log.d(TAG, "limition is " + limition);
		// limition = 0;
		uptrafficduration = limition * 1024;
		downtrafficduration = limition * 1024;
	}

	public void startTest() {
		sendBroadcast(new Intent("stopUploadData"));
		if (networkStatus && testModel == 0) {// 服务器测试
			Log.e("starttest", "start");
			uploadtrafficusage = 0;
			downloadtrafficusage = 0;

			// zj
			if (unoTest.networkInfo.getNetworkType().equals("Wi-Fi")) {
				downloadtrafficstart = TrafficStats.getTotalRxBytes()
						- TrafficStats.getMobileRxBytes();
				uploadtrafficstart = TrafficStats.getTotalTxBytes()
						- TrafficStats.getMobileTxBytes();
			} else {
				downloadtrafficstart = TrafficStats.getMobileRxBytes();
				uploadtrafficstart = TrafficStats.getMobileTxBytes();
			}

			Log.d("downloadtrafficusage", "下行消耗流量：" + downloadtrafficusage);
			Log.d("uploadtrafficusage", "上行消耗流量：" + uploadtrafficusage);

			testtimes++;

			isAbort = false;
			ping_latency = 0;
			ave_download_speed = 0;
			max_download_speed = 0;
			ave_upload_speed = 0;
			max_upload_speed = 0;

			mobileOneLatencyServerAddress = unoTest.serverInfo
					.getOneMobileLatencyTestServerAddress();
			mobileTwoLatencyServerAddress = unoTest.serverInfo
					.getTwoMobileLatencyTestServerAddress();
			wifiOneDownloadServerAddress = unoTest.serverInfo
					.getOneWifiDownloadTestServerAddress();
			mobileOneDownloadServerAddress = unoTest.serverInfo
					.getOneMobileDownloadTestServerAddress();
			mobileOneUploadServerAddress = unoTest.serverInfo
					.getOneMobileUploadTestServerAddress();
			wifiTwoDownloadServerAddress = unoTest.serverInfo
					.getTwoWifiDownloadTestServerAddress();
			mobileTwoDownloadServerAddress = unoTest.serverInfo
					.getTwoMobileDownloadTestServerAddress();
			mobileTwoUploadServerAddress = unoTest.serverInfo
					.getTwoMobileUploadTestServerAddress();

			timeOfStart = System.currentTimeMillis();
			new speedController().start();
			mainHandler.post(mTasks);
		}
	}

	public void killTest() {
		isAbort = true;
		if (TestStatus.testingStatus == TestStatus.gettingLatency)
			TestStatus.testingStatus = TestStatus.testInterruptInlatency;
		else if (TestStatus.testingStatus == TestStatus.gettingUploadSpeed)
			TestStatus.testingStatus = TestStatus.testInterruptInupload;
		else if (TestStatus.testingStatus == TestStatus.gettingDownloadSpeed)
			TestStatus.testingStatus = TestStatus.testInterruptIndownload;
		mainHandler.removeCallbacks(stopDownload);
		mainHandler.removeCallbacks(stopUpload);
		Log.e("TestStatus", "TestStatus=" + TestStatus.testingStatus);
	}

	/**
	 * 发送给主activity的消息
	 * 
	 * @param str
	 */
	public void sendToActivity(String str) {
		Intent i = new Intent(Service_Data);
		i.putExtra("ServiceState", str);
		sendBroadcast(i);
	}

	String server = "http://xugang.host033.youdnser.com/UNOTest/speedtest/random1500x1500.jpg";
	String server2 = "http://buptant.cn/UNOTest/speedtest/random1500x1500.jpg";

	public class speedController extends Thread {
		public void run() {
			sendToActivity("Button_Abort"); // 通知线程可以更改为abort

			/**
			 * 测试进入获取latency阶段
			 * */
			TestStatus.testingStatus = TestStatus.gettingLatency;

			/**
			 * latency 流程控制
			 * 
			 */
			test_controller_status = 1;//

			Latency latency = unoTest.getLatencyTest();

			latency.setServer(mobileOneLatencyServerAddress,
					mobileTwoLatencyServerAddress);
			Log.i(TAG, "setLatencyServer=" + mobileOneLatencyServerAddress
					+ "\n" + mobileTwoLatencyServerAddress);

			ping_latency = latency.getLatency();
			Log.v(TAG, "ping_latency " + ping_latency);
			sendToActivity("Latency_Finish"); // 通知Latency测试已经结束

			if (ping_latency < 0) {

				Log.v("TAG", "ping_latency " + ping_latency);
				sendToActivity("Latency_Finish"); // 通知Latency测试已经结束
				sendToActivity("Test_Finish");
				TestStatus.testingStatus = TestStatus.testInterruptInlatency;
				MainService.this.killTest();
			}

			if (!isAbort && networkStatus) {
				u = unoTest.getUploadTest();
				uisrunning = true;
				test_controller_status = 2;
				// zj
				if (unoTest.networkInfo.getNetworkType().equals("Wi-Fi")
						|| unoTest.networkInfo.getNetworkType().equals("4G")) {
					// u.setPthreadNum(5);

					u.setNetworkType(Upload.NETWORK_WIFI);
				} else {
					// u.setPthreadNum(3);
					u.setNetworkType(Upload.NETWORK_MOBILE);
				}
				u.setNetworkType(Upload.NETWORK_MOBILE);

				u.setTestTime(2);
				u.setPthreadNum(3);
				u.setUploadTraffic(uptrafficduration);
				u.setTestServer(server, server2);

				u.start();
				mainHandler.postDelayed(stopUpload, 20000);

				/**
				 * 测试进入上行测试阶段
				 * */
				// CCC
				try {
					Thread.sleep(1000);
					Log.i("ccc", "进入上行测试阶段 sleep 1000");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// CCC
				TestStatus.testingStatus = TestStatus.gettingUploadSpeed;

			}

			mainHandler.postDelayed(dTasks, 500);
		}
	};

	// 监测上行是否完成，好开始进行下行任务
	private Runnable dTasks = new Runnable() {
		public void run() {
			// ccc
			try {
				Thread.sleep(1000);
				Log.i("ccc", "进行下行测试 sleep 1000");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// ccc
			if (u.getState() == Upload.STATE_COMPLETE
					|| u.getState() == Upload.STATE_KILLED) {
				uisrunning = false; // 上行测试结束了
				ave_upload_speed = u.getAverageSpeed();
				max_upload_speed = u.getMaxSpeed();
				// Log.v("bbbbbbbbbb", "" + u.getPacketLossRatio());
				sendToActivity("Upload_Finish");
				mainHandler.removeCallbacks(mTasks);
				if (!isAbort && networkStatus) {
					d = unoTest.getDownloadTest();
					// d = new Download();
					disrunning = true;
					test_controller_status = 3;
					mainHandler.post(mTasks);
					d.setThreadNum(6);
					d.setDuration(2);
					d.setTrafficLimition(downtrafficduration);
					// 4G和Wi-Fi使用同一种下载策略
					// zj
					if (unoTest.networkInfo.getNetworkType().equals("Wi-Fi")
							|| unoTest.networkInfo.getNetworkType()
									.equals("4G")) {
						// d.setMultiTestServer(wifiOneDownloadServerAddress,
						// wifiTwoDownloadServerAddress);
					} else {
						Log.v(mobileOneDownloadServerAddress,
								mobileTwoDownloadServerAddress);
						// d.setMultiTestServer(mobileOneDownloadServerAddress,
						// mobileTwoDownloadServerAddress);
					}
					d.setMultiTestServer(server, server2);
					d.start();
					mainHandler.post(mTasksControler);
					mainHandler.postDelayed(stopDownload, 20000);

					/**
					 * 测试进入下行测试阶段
					 * */
					TestStatus.testingStatus = TestStatus.gettingDownloadSpeed;
					ipInformation = unoTest.networkInfo.getTheIpinfo();
					Log.v("MainService", "get the ip info=" + ipInformation);
				}
			} else {
				// mainHandler.postDelayed(dTasks, 300);
				mainHandler.postDelayed(dTasks, 500);
			}
		}
	};

	private Runnable mTasks = new Runnable() {
		public void run() {
			// Log.v("mTasks","mTasks"+isAbort);
			Intent i = new Intent(Service_Data);
			i.putExtra("ServiceState", "currentSpeedUpdate");
			if (disrunning && d.getState() == Download.STATE_RUNNING) {
				currentSpeed = d.getCurrentSpeed();
				// 观察情况
				Log.v("mainService", "mainService currentSpeed=" + currentSpeed);
				if (!isAbort) {
					sendBroadcast(i);
				}
			}
			if (uisrunning && u.getState() == Upload.STATE_RUNNING) {
				currentSpeed = u.getCurrentSpeed();
				if (!isAbort) {
					sendBroadcast(i);
				}
			}
			// mainHandler.postDelayed(mTasks, 100);// /0.3s更新一次UI
			mainHandler.postDelayed(mTasks, 500);// /0.3s更新一次UI
		}
	};

	private Runnable mTasksControler = new Runnable() {
		public void run() {
			if (d.getState() == Download.STATE_COMPLETE
					|| d.getState() == Download.STATE_KILLED) {// 下行程序结束了
				/**
				 * 测试进入结束阶段，进行存储数据
				 * */
				mainHandler.removeCallbacks(stopDownload);
				mainHandler.removeCallbacks(stopUpload);

				TestStatus.testingStatus = TestStatus.oneTestCompleted;

				isAbort = true;

				disrunning = false;
				ave_download_speed = d.getAveSpeed();
				max_download_speed = d.getMaxSpeed();
				if (ave_download_speed < 0) {
					ave_download_speed = 0;
				}
				if (max_download_speed < 0) {
					max_download_speed = 0;
				}
				Log.v(TAG + ave_download_speed, "" + max_download_speed);

				// linjianxin
				if (unoTest.networkInfo.getNetworkType().equals("Wi-Fi")) {
					downloadtrafficusage = TrafficStats.getTotalRxBytes()
							- TrafficStats.getMobileRxBytes()
							- downloadtrafficstart;
					uploadtrafficusage = TrafficStats.getTotalTxBytes()
							- TrafficStats.getMobileTxBytes()
							- uploadtrafficstart;
				} else {
					downloadtrafficusage = TrafficStats.getMobileRxBytes()
							- downloadtrafficstart;
					uploadtrafficusage = TrafficStats.getMobileTxBytes()
							- uploadtrafficstart;
				}
				if (downloadtrafficusage < 0)
					downloadtrafficusage = 0;
				if (uploadtrafficusage < 0)
					uploadtrafficusage = 0;
				// 公式转换 B转换成Mb

				downloadtrafficusage = downloadtrafficusage
						/ (1024 * 1024 * 1.0);
				uploadtrafficusage = uploadtrafficusage / (1024 * 1024 * 1.0);

				double downloadtraffic = Double.valueOf(String.format("%.2f",
						downloadtrafficusage));
				double uploadtraffic = Double.valueOf(String.format("%.2f",
						uploadtrafficusage));

				upload_traffic = uploadtraffic * 1024;
				download_traffic = downloadtraffic * 1024;
				if (ave_download_speed > 0 && ave_upload_speed > 0) {
					control c = new control();// liuyan
					c.start();
				}
				sendToActivity("Download_Finish");
				Intent i = new Intent(Service_Data);
				i.putExtra("ServiceState", "Test_Finish");
				sendBroadcast(i);

				mainHandler.removeCallbacks(mTasks);
				// 进行服务器选择
				if (testtimes % 7 == 5) {
					unoTest.serverInfo.startAutoServerSelection();
				}
//				if (ave_download_speed > 0 && ave_upload_speed > 0) {
					lat = GraphActivity.rPointX;
					lon = GraphActivity.rPointY;
					floor = GraphActivity.floor;
					Log.v("position", "lat" + lat);
					Log.v("positon","lon" + lon);
					Log.v("positon","floor" + floor);
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String date = formatter.format(curDate);
					FileExport fileexport = new FileExport(MainService.this);
					StringBuilder sb = new StringBuilder();
					sb.append(date + ",")
							.append(unoTest.networkInfo.getSimOperatorName() + ",")
							.append(unoTest.networkInfo.getNetworkType() + ",")
							.append(ping_latency + "ms" + ",")
							.append((float) (Math.round(ave_upload_speed
											* 1024 * 8 / 1048576f * 100)) / 100
									+ "Mbps" + ",")
							.append((float) (Math.round(max_upload_speed
											* 1024 * 8 / 1048576f * 100)) / 100
									+ "Mbps" + ",")
							.append((float) (Math.round(ave_download_speed
											* 1024 * 8 / 1048576f * 100)) / 100
									+ "Mbps" + ",")
							.append((float) (Math.round(max_download_speed
											* 1024 * 8 / 1048576f * 100)) / 100
									+ "Mbps" + ",").append(lat + ",")
							.append(lon + ",")
							.append(floor + ";");
					fileexport.storeOnline(fileName, sb.toString(), true);
					mydatabase.open();
					mydatabase.insertTestData(timeOfStart, ave_download_speed,
							max_download_speed, ave_upload_speed,
							max_upload_speed, ping_latency, uploadtraffic,
							downloadtraffic, networkType,
							unoTest.networkInfo.getSimOperatorName(),
							unoTest.networkInfo.getTheIpinfo(), testModel, lat
									+ "", lon + "", floor);
					mydatabase.fetchAllData();
					mydatabase.close();
//				}

			} else {
				mainHandler.postDelayed(mTasksControler, 500);
			}
		}
	};

	public class control extends Thread {
		public void run() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("ping", ping_latency);
				jsonObject.put("ave_downloadSpeed", "" + ave_download_speed);
				jsonObject.put("max_downloadSpeed", "" + max_download_speed);
				jsonObject.put("ave_uploadSpeed", "" + ave_upload_speed);
				jsonObject.put("max_uploadSpeed", "" + max_upload_speed);
				jsonObject.put("gps_lat",
						"" + lat);
				jsonObject.put("gps_lon",
						"" + lon);
//				jsonObject.put("gps_floor",
//						"" + floor);
				jsonObject.put("ant_version", TestStatus.Con_version);
				jsonObject.put("detail", TestStatus.locationTag);
				jsonObject.put("mobile_type",
						unoTest.networkInfo.getNetworkStandard());
				jsonObject.put("location_type",
						unoTest.locationInfo.getProviderName()
								+ unoTest.locationInfo.getBaiDuCoorType());
				jsonObject.put("server_url",
						unoTest.serverInfo.getTestServers());
				jsonObject.put("networkType",
						unoTest.networkInfo.getNetworkType());
				jsonObject.put("upload_traffic", upload_traffic);
				jsonObject.put("download_traffic", download_traffic);
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String date = formatter.format(curDate);
				jsonObject.put("time_client_test", date);
				jsonObject.put("operator_name",
						unoTest.networkInfo.getSimOperatorName());
				jsonObject.put("imei", "" + unoTest.hardwareInfo.getIMEI());
				jsonObject.put("cell_id", "" + unoTest.networkInfo.getCellID());

				if (unoTest.networkInfo.getNetworkType() == "Wi-Fi") {
					jsonObject.put("wifi_bss_id",
							unoTest.networkInfo.getWifiBSSID());

					if (!ipInformation.equals("正在获取"))
						jsonObject.put("operator_name", ipInformation);
					else
						jsonObject.put("operator_name", "未获取");
					jsonObject.put("rssi", unoTest.networkInfo.getWifiRssi());
				} else {
					jsonObject.put("wifi_bss_id", "null");
					jsonObject.put("rssi", unoTest.networkInfo.getRssi());
				}

			} catch (JSONException e) {
				e.printStackTrace();
				Log.v("jsonObject2.3", "jsonObject erro");
			}
//			String testInfoUrl = "http://xugang.host033.youdnser.com/score/insert_testinfo_rank.php";
			String testInfoUrl = "http://buptant.cn/anttest/applist-traffic-testinfo/insert_testinfo_db2.php";
			UploadData u = new UploadData(testInfoUrl, jsonObject);
			String re = u.upData();
		}
	}

	public int getNetworkSpeed() {
		return currentSpeed + 3;
	}

	/**
	 * 获取网络类型
	 * */
	private class networkListen extends Thread {
		public void run() {
			ConnectivityManager();
			providersname = unoTest.networkInfo.getSimOperatorName();
			while (true) {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ConnectivityManager();
			}
		}
	}

	/**
	 * 心跳任务
	 * **/
	private Runnable stopDownload = new Runnable() {
		public void run() {
			Log.e("", "try to stop download ");
			if (d != null && (d.getState() == Download.STATE_RUNNING))
				d.stop();
		}
	};
	private Runnable stopUpload = new Runnable() {
		public void run() {
			Log.e("", "try to stop upload ");
			if (u != null && (u.getState() == Download.STATE_RUNNING))
				u.stop();
		}
	};

	// 网络监控类
	public void ConnectivityManager() {

		ConnectivityManager connManager = (ConnectivityManager) this
				.getSystemService(CONNECTIVITY_SERVICE);
		// 获取代表联网状态的NetWorkInfo对象
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

		// 获取当前的网络连接是否可用
		if (networkInfo == null) {
			networkStatus = false;
			killTest();// 这个函数有待完善
			isAbort = true;// /网络不好的时候中断测量
			sendToActivity("Network_Interrupt");
			// Log.i("网络状态通知", "当前的网络连接不可用");
		} else {
			boolean available = networkInfo.isAvailable();
			if (available) {
				networkStatus = true;
				sendToActivity("Network_Reconnected");
				ipInformation = unoTest.networkInfo.getTheIpinfo();
				// Log.i("网络状态通知", "当前的网络连接可用");
			} else {
				networkStatus = false;
				killTest();// 这个函数有待完善
				isAbort = true;// /网络不好的时候中断测量
				sendToActivity("Network_Interrupt");
				Log.i("网络状态通知", "当前的网络连接不可用");
			}
		}
		try {
			State state = connManager.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();
			if (State.CONNECTED == state) {
				link_type = 0;
				networkType = unoTest.networkInfo.getNetworkGeneration();
				// if (networkType.equals("4G")) {
				// SpeedTestFragment.isMb = true;
				// }
			}
		} catch (Exception e) {

		}
		try {
			State state = connManager.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();
			if (State.CONNECTED == state) {
				link_type = 1;
				networkType = "Wi-Fi";
				// SpeedTestFragment.isMb = true;
			}
		} catch (Exception e) {

		}
	}
}
