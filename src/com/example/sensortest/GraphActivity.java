package com.example.sensortest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.docomo.Data.FileExport;
import com.docomo.Data.MyDatabase; 

import edu.bupt.anttest.MainService;
import edu.bupt.anttest.MainService.LocalBinder;
import edu.bupt.unotest.UNOTest;
import nisargpatel.deadreckoning.graph.ScatterPlot;

public class GraphActivity extends Activity implements SensorEventListener,
		LocationListener {

	private static final long GPS_SECONDS_PER_WEEK = 511200L;

	private static final float GYROSCOPE_INTEGRATION_SENSITIVITY = 0.0025f;

	private static final String FOLDER_NAME = "Dead_Reckoning/Graph_Activity";
	private static final String[] DATA_FILE_NAMES = { "Initial_Orientation",
			"Linear_Acceleration", "Gyroscope_Uncalibrated",
			"Magnetic_Field_Uncalibrated", "Gravity", "XY_Data_Set" };
	private static final String[] DATA_FILE_HEADINGS = {
			"Initial_Orientation",
			"Linear_Acceleration" + "\n" + "t;Ax;Ay;Az;findStep",
			"Gyroscope_Uncalibrated" + "\n"
					+ "t;uGx;uGy;uGz;xBias;yBias;zBias;heading",
			"Magnetic_Field_Uncalibrated" + "\n"
					+ "t;uMx;uMy;uMz;xBias;yBias;zBias;heading",
			"Gravity" + "\n" + "t;gx;gy;gz",
			"XY_Data_Set"
					+ "\n"
					+ "weekGPS;secGPS;t;strideLength;magHeading;gyroHeading;originalPointX;originalPointY;rotatedPointX;rotatedPointY" };

	private ScatterPlot scatterPlot;

	private LinearLayout mLinearLayout;
	private float firstGro;
	private LocationManager locationManager;
	private float firstMag;
	float[] gyroBias;
	float[] magBias;
	float[] currGravity; // current gravity
	float[] currMag; // current magnetic field
	int nn = 0;
	private boolean isRunning;
	private boolean isCalibrated;
	private boolean usingDefaultCounter;
	private boolean areFilesCreated;
	private float strideLength;
	private float gyroHeading;
	private float magHeading;
	private float weeksGPS;
	private float secondsGPS;

	private long startTime;
	private boolean firstRun;

	private float initialHeading;
	private SensorManager sensorManager;
	private Sensor gSensor;
	private Sensor mSensor;
	private Sensor hSensor;
	private float[] mGData = new float[3];// 加速度传感器
	private float[] mMData = new float[3];// 磁力传感器
	private float[] mTData = new float[3];// 陀螺仪
	private float[] mR = new float[16];
	private float[] mI = new float[16];
	private float[] mOrientation = new float[3];
	private float[][] a_LCS = new float[3][1];
	private float[][] m_LCS = new float[3][1];
	private float[][] g_LCS = new float[3][1];
	private int countHPF = 0;
	private int countLPF = 0, count1 = 0, count2 = 0, count3 = 0;
	private int t = 0;
	private float g = SensorManager.STANDARD_GRAVITY;
	private TextView text_isR;
	private TextView text_mR;
	private TextView text_orientation;
	private TextView showlg;
	private float[] afterHPF = new float[360000];
	private float[] afterLPF = new float[360000];
	private float[] afterLPF_negative = new float[360000];
	private float hGro = 0f;// 陀螺仪
	private float[] hMag = new float[360000];// 磁力计
	private float[] heading = new float[360000];// 方向角
	private float[] time = new float[360000];
	private int j = 6;
	private int W = 2 * j - 1;
	private int N = 2 * j;
	private boolean isFirstStep = true;
	private float a_pp = 0.75f;
	private int step = 0;
	private float a_thresh = 3.230f;
	private boolean can_add = true;
	private int pre_add = 0;
	private float totalLength = 0;
	private String FILE_PATH = null;
	private float hDecline;
	private float x, y;
	private float[][] G = { { 0.0f }, { 0.0f }, { g } };
	FileOutputStream out = null;
	String temp = null;
	private LocationManager locManager = null;
	MovingAverage movingAverage;// 陀螺仪moving window

	// 测速相关
	public Button start;
	public Button history;
	public Button addpoint;
	// public static double lat = 116.32603454589844;
	// public static double lon = 39.9664192199707;
	public static int floor = 1;
	public static float rPointX;
	public static float rPointY;

	MyDatabase mydatabase;
	Cursor mycursor;

	MainService mainService;
	static UNOTest unoTest;
	MyBroadcastReceiver myBroadcastReceiver;
	static int speedlevel = 0;// /速度等级，-2表示正在运行，-1表示停止
	int state_startTest = 0; // 0代表显示test 1代表显示abort
	public float ave_download_speed = 0;
	public float max_download_speed = 0;
	public float ave_upload_speed = 0;
	public float max_upload_speed = 0;
	public int ping_latency = 0;

	private Context context;
	private boolean isBound = false;
	
	public Handler mHandler,handler;

	float azimuth;
	float roll;
	float pitch;
	float[][] mR1 = new float[3][3];
	public static final int TIME_CONSTANT = 20; 
	
	private Timer fuseTimer = new Timer(); 
	private boolean isTimerReady = false;
	private String[] filename = {"xy","hMag","a_GCS_z--raw","a_GCS_z--HPF","a_GCS_z--LPF","g_GCS_z--raw",
								"hMag[t]--movingAverage","hGro","heading","steplength","totalLength","xycontinuous","TimeStamp"};
	private FileOutputStream[] fout = new FileOutputStream[13];
	
	
	
	
	
	
	
	
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		 context = GraphActivity.this;
//		context = this;
		isRunning = false;
 
		mLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutGraph);
		text_orientation = (TextView) findViewById(R.id.orientation);

		// setting up graph with origin
		scatterPlot = new ScatterPlot("Position");
		scatterPlot.addPoint(0, 0);
		mLinearLayout
				.addView(scatterPlot.getGraphView(getApplicationContext()));

		// starting GPS location tracking
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, GraphActivity.this);

		// starting sensors
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(GraphActivity.this,
				sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// heading
		hSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		sensorManager.registerListener(this, gSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, hSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		movingAverage = new MovingAverage(15);// 陀螺仪滤波

		for (int i = 0; i <= j - 2; i++) {
			afterLPF[i] = 0;
			afterLPF_negative[i] = 0;

		}

		// 测速相关
		unoTest = new UNOTest(context);
		try {
			IntentFilter intentFilter = new IntentFilter(
					MainService.Service_Data);
			myBroadcastReceiver = new MyBroadcastReceiver();
			this.registerReceiver(myBroadcastReceiver, intentFilter);
		} catch (Exception e) {
			e.getStackTrace();
		}

		Intent intent = new Intent();
		intent.setClass(GraphActivity.this, MainService.class);
		this.bindService(intent, connection, Context.BIND_AUTO_CREATE);

		// zj
		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				wrtString(fout[12]);
				mainService.startTest();
				// 在测速点描绘位置
				// drawPosition(result.x, result.y, result.z);
			}
		});

		// history

		history = (Button) findViewById(R.id.history);
		history.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent it = new Intent(GraphActivity.this,
						HistoryActivity.class);
				startActivity(it);
			}
		});

		
		addpoint = (Button) findViewById(R.id.addpoint);
		addpoint.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
//				if(can_add){
					scatterPlot.addPoint(rPointX, rPointY);  
//					wrtfloat(fout[0], rPointX, rPointY);
						mLinearLayout.removeAllViews();
						mLinearLayout.addView(scatterPlot
								.getGraphView(getApplicationContext()));
//				}
			}
		});
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
		{
			try {
				FILE_PATH = Environment.getExternalStorageDirectory()
						.getCanonicalPath().toString()
						+ "/cccc";
				File files = new File(FILE_PATH);
				if (!files.exists()) {
					files.mkdirs();
				}
				System.out.println(FILE_PATH);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		/*for(int i = 0; i < filename.length;i++)
			wrt(filename[i],i);*/
		wrt(filename[11],11);
		 
	}
 
	private void  wrt(String filename, int i ){ 
		File dir = new File(FILE_PATH + "/");
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " "  
                + "hh:mm:ss");   
		File file = new File(dir,  tempDate.format(new java.util.Date()).toString() + filename + ".txt");
//		Log.d("file", "open " + filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
//				Log.d("file", "create " + filename);
			} catch (IOException e) {
				Log.d("file", filename);
				e.printStackTrace();
			}
		}
		System.out.println(file.exists());
		try {
			fout[i] = new FileOutputStream(file); 
			//Log.d("file", "new stream " + filename);
			System.out.println(fout);
		} catch (FileNotFoundException e) {
			Log.d("file", "Stream "+filename);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 
	private void wrtfloat(FileOutputStream fout, float f) {
		String temp = f + " ";
		try {
			fout.write(temp.getBytes());
			Log.d("file", "write " + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void wrtfloat(FileOutputStream fout, float f1, float f2) {
		String temp = f1 + "," + f2 + ";";
		Log.d("file", "write " + filename);
		
		try {
			fout.write(temp.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void wrtString(FileOutputStream fout) {
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd" + " "  
                + "hh:mm:ss");   
		String temp = tempDate.format(new java.util.Date()).toString() + ","; 
		try {
			fout.write(temp.getBytes());
			Log.d("file", "write " + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                100, TIME_CONSTANT); 
		  
		 handler = new Handler(); 
	}










	private ServiceConnection connection = new ServiceConnection() {
		// 当Activity和service绑定的时候会调用
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			// 根据传过来的IBinder重建messenger对象，这样的话，就建成了service和activity的通道了
			// Toast.makeText(context, "onServiceConnected", 1000).show();

			isBound = true;
			LocalBinder binder = (LocalBinder) service;
			mainService = binder.getService();
			mainService.mContext = context;
			Log.d("service connection", "bind succ");
		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			isBound = false;
			Toast.makeText(GraphActivity.this, "onServiceDisConnected", 1000)
					.show();
		}
	};

	
	
	
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				/**
				 * TextView networkText; TextView networkValue; TextView
				 * latencyText; TextView latencyValue; TextView uploadText;
				 * TextView uploadValue; TextView downloadText; TextView
				 * downloadValue;
				 * */
				String a = intent.getExtras().getString("ServiceState");

				if (a.equals("Latency_Finish")) {
					ping_latency = mainService.ping_latency;
					Toast.makeText(GraphActivity.this,
							"时延 " + mainService.ping_latency + "ms",
							Toast.LENGTH_LONG).show();
				} else if (a.equals("Upload_Finish")) {
					ave_upload_speed = mainService.ave_upload_speed;
					max_upload_speed = mainService.max_upload_speed;
					if (mainService.ave_upload_speed < 2) {
						Toast.makeText(GraphActivity.this, "上行速率未获得",
								Toast.LENGTH_LONG).show();
						Toast.makeText(GraphActivity.this, "上行峰值速率未获得",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(
								GraphActivity.this,
								"上行平均速率"
										+ (float) (Math
												.round(mainService.ave_upload_speed
														* 1024
														* 8
														/ 1048576f
														* 100)) / 100 + "Mbps",
								Toast.LENGTH_LONG).show();
						Toast.makeText(
								GraphActivity.this,
								"上行峰值速率"
										+ (float) (Math
												.round(mainService.max_upload_speed
														* 1024
														* 8
														/ 1048576f
														* 100)) / 100 + "Mbps",
								Toast.LENGTH_LONG).show();
					}

				} else if (a.equals("Download_Finish")) {
					ave_download_speed = mainService.ave_download_speed;
					max_download_speed = mainService.max_download_speed;
					if (mainService.ave_download_speed < 2) {
						Toast.makeText(GraphActivity.this, "下行速率未获得",
								Toast.LENGTH_LONG).show();
						Toast.makeText(GraphActivity.this, "下行峰值速率未获得",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(
								GraphActivity.this,
								"下行平均速率"
										+ (float) (Math
												.round(mainService.ave_download_speed
														* 1024
														* 8
														/ 1048576f
														* 100)) / 100 + "Mbps",
								Toast.LENGTH_LONG).show();
						Toast.makeText(
								GraphActivity.this,
								"下行峰值速率"
										+ (float) (Math
												.round(mainService.max_download_speed
														* 1024
														* 8
														/ 1048576f
														* 100)) / 100 + "Mbps",
								Toast.LENGTH_LONG).show();
					}
				} else if (a.equals("Test_Finish")) {
					FileExport fileexport = new FileExport(context);
					StringBuilder sb = new StringBuilder();
					sb.append(ping_latency + "\t")
							.append(ave_upload_speed + "\t")
							.append(max_upload_speed + "\t")
							.append(ave_download_speed + "\t")
							.append(max_download_speed + "\t")
							.append(rPointX + "\t").append(rPointY + "\t")
							.append(floor + "\t");
					fileexport.storeOnline("indoorSpeed", sb.toString(), true);
					state_startTest = 0;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	
	
	@Override
	protected void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
		locationManager.removeUpdates(this); 
		for(int i = 0; i < filename.length;i++){
			try {
				fout[i].close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}
	} 
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		int type = event.sensor.getType();
		float data[];
		if (type == Sensor.TYPE_ACCELEROMETER) {
			data = mGData;
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			data = mMData;
		} else if (type == Sensor.TYPE_GYROSCOPE) {
			data = mTData;
		}

		else {
			return;
		}

		for (int i = 0; i < 3; i++) {
			data[i] = event.values[i];
		}
		if (t == 0) {
			time[0] = event.timestamp / 1000000000.0f;

			// hGro[0] = 0;
		}

		// 根据设备传输过来的向量数据计算倾斜矩阵mR以及旋转矩阵mI
		SensorManager.getRotationMatrix(mR, mI, mGData, mMData);
		// 根据旋转矩阵mR计算出设备的方向
		SensorManager.getOrientation(mR, mOrientation);
		/**
		 * values[0] ：azimuth 方向角，但用（磁场+加速度）得到的数据范围是（-180～180）,
		 * 也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西。
		 * 而直接通过方向感应器数据范围是（0～359）360/0表示正北，90表示正东，180表示正南，270表示正西。 values[1]
		 * pitch 倾斜角 即由静止状态开始，前后翻转 values[2] roll 旋转角 即由静止状态开始，左右翻转
		 */
		// 手机绕z轴旋转的度数
		  azimuth = (float) Math.toDegrees(mOrientation[0]);
		// 手机绕x轴旋转的度数
		  pitch = (float) Math.toDegrees(mOrientation[1]);
		// 手机绕y轴旋转的度数
		  roll = (float) Math.toDegrees(mOrientation[2]); 

		 mR1[0][0] = mR[0];
		 mR1[0][1] = mR[1];
		 mR1[0][2] = mR[2];
		 mR1[1][0] = mR[4];
		 mR1[1][1] = mR[5];
		 mR1[1][2] = mR[6];
		 mR1[2][0] = mR[8];
		 mR1[2][1] = mR[9];
		 mR1[2][2] = mR[10]; 

		for (int i = 0; i < 3; i++) {
			a_LCS[i][0] = mGData[i];
		}
		for (int i = 0; i < 3; i++) {
			m_LCS[i][0] = mMData[i];
		}
		for (int i = 0; i < 3; i++) {
			g_LCS[i][0] = mTData[i];
		}
		time[t] = event.timestamp / 1000000000.0f;
		 
		if(isTimerReady){
              Message message = Message.obtain();
              message.what = 111;  
              message.arg1 = t;  
              mHandler.sendMessage(message);
              Log.d("NNNNNNNNNNNNNNNNNNNNN", t + "");  
              Log.d("Main thread", t + "");
              
		}    
		t++; 
		
	}

	 
	
	
	 class calculateFusedOrientationTask extends TimerTask {
//		 private Looper looper;
		 
	        public void run() {
	        	isTimerReady = true;
	        	 Looper.prepare();
//	        	 looper = Looper.myLooper();
	        	 
	        	 mHandler = new Handler( ){//2、绑定handler到CustomThread实例的Looper对象
	                 public void handleMessage (Message msg) {//3、定义处理消息的方法
	                     switch(msg.what) {
	                     case 111:
	                          int t = msg.arg1;
	                          
	                       // ///////////////////////////////////////////////////////////////////////////////////////////
	          				// heading estimation part magnetometer
	          				// float[][] m_GCS = multiple(mRR,m_LCS);
	          				// hMag[t] = (float) atan2(-m_GCS[1][0], m_GCS[0][0]);
	          	        	 
	          				hMag[t] = azimuth; 
	          				if (getGeoNorthDeclination() != 0) {
	          					hDecline = getGeoNorthDeclination();
	          					// System.out.println(hDecline+"");
	          				} else {
	          					hDecline = -(6 + 5 / 60);
	          				}
	          				hMag[t] -= hDecline;
	          				movingAverage.pushValue(hMag[t]);
	          				hMag[t] = movingAverage.getValue();
//	          				wrtfloat(fout[6], hMag[t]);
//	          				wrtfloat(fout[1],hMag[t]);
	          				Log.d("hMag", hMag[t] + "");
	          				// ///////////////////////////////////////////////////////////////////////////////////////////
	          	        	

	          	    		// ///////////////////////////////////////////////////////////////////////////////////////////
	          	    		// heading estimation part gyroscope
	          	    		// 添加gyoscope校准！！！

	          	    		float[][] a_GCS = multiple(mR1, a_LCS);
	          	    		float a_GCS_z = a_GCS[2][0];
//	          	    		wrtfloat(fout[2], a_GCS_z);
	          	    		afterHPF[t] = HPF(a_GCS_z);
//	          	    		wrtfloat(fout[3], afterHPF[t]);
	          	    		

	          	    		if (Math.abs(afterHPF[t]) > 0.2f)
	          	    			countHPF++;
	          	    		if (t >= W - 1) {
	          	    			afterLPF[t - j + 1] = LPF(afterHPF);
//	          	    			wrtfloat(fout[4], afterLPF[t - j + 1]);
	          	    			afterLPF_negative[t - j + 1] = 0 - afterLPF[t - j + 1];
	          	    			// write2File1(afterLPF[t-j+1]);
	          	    			if (Math.abs(afterLPF[t - j + 1]) > 0.2f)
	          	    				countLPF++;
	          	    			// text_isR.setText("afterHPF:"+afterHPF[t]+"\nafterLPF:"+afterLPF[t-j+1]+"\nHPF:"+countHPF+"\nLPF:"+countLPF+"\n"+t);
	          	    		}

	          	    		if (t >= j + N - 1) // N?
	          	    		{

	          	    			// float[][] revMR1 = reverse(mRR);//旋转矩阵转置
	          	    			// float[][] gt = multiple(revMR1,G);
	          	    			float[][] g_GCS_1 = multiple(mR1, g_LCS);
	          	    			// float absGT = (float)Math.sqrt(Math.pow(gt[0][0],2) +
	          	    			// Math.pow(gt[1][0],2) + Math.pow(gt[2][0], 2));

	          	    			// float[][] g_GCS = multiple(reverse(g_LCS),gt);

	          	    			// Log.d("g_GCS", Arrays.toString(g_GCS[0]));

	          	    			// float g_GCS_z = g_GCS[0][0]/ absGT;
	          	    			float g_GCS_z = g_GCS_1[2][0];
//	          	    			wrtfloat(fout[5], g_GCS_z);
//	          	    			movingAverage.pushValue(g_GCS_z);
//	          	    			g_GCS_z = movingAverage.getValue();
//	          	    			wrtfloat(fout[6], g_GCS_z);
//	          	    			write2File(g_GCS_z);
	          	    			if (Math.abs((-(float) Math.toDegrees(g_GCS_z
	          	    					* (time[t] - time[t - 1])))) < 0.18f)// 陀螺仪时间和累加
	          	    				hGro += 0;
	          	    			else
	          	    				hGro += (-(float) Math.toDegrees(g_GCS_z
	          	    						* (time[t] - time[t - 1])));

	          	    			Log.d("hGro", hGro + "");

	          	    			float w_pre, w_mag, w_gyro;// 权重参数
	          	    			float w_pmg, w_mg, w_pg;
	          	    			float h_cor_S, h_mag_S;
	          	    			float h_cor_C, h_mag_C;
	          	    			w_pre = 2.0f;
	          	    			w_mag = 1.0f;
	          	    			w_gyro = 2.0f;
	          	    			w_pmg = 1 / (w_pre + w_mag + w_gyro);
	          	    			w_mg = 1 / (w_mag + w_gyro);
	          	    			w_pg = 1 / (w_pre + w_gyro);
	          	    			h_cor_C = 5.0f;
	          	    			h_mag_C = 2.0f;
	          	    			// h_cor_C = (float) Math.toRadians(5.0);
	          	    			// h_mag_C = (float) Math.toRadians(2.0);
	          	    			Log.d("h_cor_C", h_cor_C + "");
	          	    			Log.d("h_mag_C", h_mag_C + "");

	          	    			if (t == j + N - 1) {
	          	    				for (int i = N - 1; i <= j + N - 1; i++)
	          	    					firstMag += hMag[i];
	          	    				firstMag = firstMag / (j + 1);
	          	    			}

	          	    			if (t == j + N - 1) {
	          	    				hGro = firstMag;
	          	    				heading[t - 1] = firstMag;

	          	    			}

	          	    			if (hGro > 180-hDecline) {
	          	    				hGro = hGro - 360;
	          	    			}
	          	    			if (hGro < -180-hDecline) {
	          	    				hGro += 360;
	          	    			}
//	          	    			wrtfloat(fout[7],hGro);
	          	    			// write2File(hGro);

	          	    			h_mag_S = Math.abs(hMag[t] - hMag[t - 1]);
	          	    			h_cor_S = Math.abs(hMag[t] - hGro);

	          	    			if (h_cor_S <= h_cor_C && h_mag_S <= h_mag_C) {
	          	    				heading[t] = w_pmg
	          	    						* (w_pre * heading[t - 1] + w_mag * hMag[t] + w_gyro
	          	    								* hGro);
	          	    				Log.d("wwwwwww", "111111111111111");

	          	    			} else if (h_cor_S <= h_cor_C && h_mag_S > h_mag_C) {
	          	    				heading[t] = w_mg * (w_mag * hMag[t] + w_gyro * hGro);
	          	    				Log.d("wwwwwww", "22222222222222222");
	          	    			} else if (h_cor_S > h_cor_C && h_mag_S <= h_mag_C) {
	          	    				heading[t] = heading[t - 1];
	          	    				Log.d("wwwwwww", "333333333333333");
	          	    			} else if (h_cor_S > h_cor_C && h_mag_S > h_mag_C) {
	          	    				heading[t] = w_pg * (w_pre * heading[t - 1] + w_gyro * hGro);
	          	    				Log.d("wwwwwww", "44444444444444");
	          	    			} 
//	          	    			wrtfloat(fout[8], heading[t]);
//	          	    			text_orientation.setText("Mag:" + hMag[t] + "\nGro:" + hGro
//	          	    					+ "\nheading:" + heading[t] + "\n" + "\nheadingdiff:"
//	          	    					+ (heading[t] - firstMag));
	          	    			
	          	    			// text_orientation.setText("m_GCS_z:"+m_GCS[2][0]+"\nz:"+azimuth+"\nm_GCS_y:"+m_GCS[1][0]+"\ny:"+roll+"\nm_GCS_x"+
	          	    			// m_GCS[0][0]+"\nx:"+pitch+"\nhDecline:"+hDecline+"\n");
	          	    			// //////////////////////////////////////////////////////////////////////////////
	          	    			// text_orientation.setText("1: "+g_GCS_1[2][0]+"2 :"+g_GCS_z);

	          	    			// write2File(heading[t]);

	          	    			if (findPeak1(afterLPF, 0) == 1)
	          	    				count1++;// 由条件1得到的步数
	          	    			if (ensurePP(afterLPF, 0) == 1)
	          	    				count2++;// 由条件2得到的步数
	          	    			if (ensureSlope(afterLPF, 0) == 1)
	          	    				count3++;// 由条件3得到的步数
	          	    			// text_mR.setText("step is:"+step+"\n"+count1+"\n"+count2+"\n"+count3);
	          	    			if ((t - pre_add) > 7)
	          	    				can_add = true;
	          	    			if (findPeak1(afterLPF, 0) * ensurePP(afterLPF, 0)
	          	    					* ensureSlope(afterLPF, 0) == 1) {
	          	    				if (can_add) {
	          	    					step++;
	          	    					totalLength = totalLength + calcuDis();
//	          	    					wrtfloat(fout[9], calcuDis());
//	          	    					wrtfloat(fout[10], totalLength);
	          	    					// x += calcuDis()*Math.sin(Math.toRadians(heading[t]));
	          	    					// y += calcuDis()*Math.cos(Math.toRadians(heading[t]));
	          	    					// showlg.setText("stepLength:"+calcuDis()+"\ntotalLength:"+totalLength+"\nx:"+x+"\ny:"+y);
	          	    					can_add = false;
	          	    					pre_add = t;
	          	    					float oPointX = scatterPlot.getLastYPoint();
	          	    					float oPointY = -scatterPlot.getLastXPoint();
	          	    					 
	          	    					// calculating XY points from heading and stride_length
	          	    					oPointX += 2.0f * calcuDis()
	          	    							* Math.cos(Math.toRadians(heading[t] - firstMag));
	          	    					oPointY += 2.0f * calcuDis()
	          	    							* Math.sin(Math.toRadians(heading[t] - firstMag));

	          	    					// //rotating points by 90 degrees, so north is up
	          	    					rPointX = -oPointY;
	          	    					rPointY = oPointX;
	          	    					wrtfloat(fout[11], rPointX, rPointY);
//	          	    					mLinearLayout.removeAllViews();
//	          	    					mLinearLayout.addView(scatterPlot
//	          	    							.getGraphView(getApplicationContext()));
	          	    				}

	          	    			}

	          	    		}

	          	    		// text_mR.setText(""+mGravityData[0]+"\n"+mGravityData[1]+"\n"+mGravityData[2]+"\n");
	          	    		// text_mR.setText(display(mR1));

	          	    		// 坐标系：android.hardware.SensorManager.getOrientation()方法所示的坐标系
	          	    		// 手机旋转姿势的确定：手机屏幕向上水平放置时，手机头部向北azimuth为0度，头部向正南aimuth为180度/-180度，
	          	    		// 头部向正西azimuth为-90度，头部向正东azimuth为90度，此时pitch和roll的值基本为零，因为此时手机可能不是绝对水平
	          	    		// (注意：可能是因为传感器厂家的不同，此时手机头部向正南时azimuth显示为0度，向正北时azimuth显示为180度/-180度)
	          	    		// 手机屏幕水平向上放置时，手机尾部抬起至垂直于地面时，pitch的值0-90度，继续翻转至手机屏幕水平向下，pitch的值90-0度
	          	    		// 手机屏幕水平向上放置时，手机头部抬起至垂直于地面时，pitch的值0值-90度，继续翻转至手机屏幕水平向下时，pitch的值-90-0度
	          	    		// 手机屏幕水平向上放置时，手机右侧向上抬起至手机屏幕水平向下，roll的值：0至-180度，
	          	    		// 手机左侧向上抬起至屏幕水平向下，roll的值：0-180度 
	          	    		  
	          	    		Message message = Message.obtain();
	          	    		message.what = 222;  
	          	    		message.arg1 = t;   
//	          	    		message.obj = can_add;
	          	    		mHandler1.sendMessage(message); 
	          	    		break;
	                     
	                     default:
	                    	 break;
	                     }  
	                     
	                 }
	             }; 
	             Looper.loop();
	    		
	        }
	    }
	
	 
	@Override
	public void onLocationChanged(Location location) {
		long GPSTimeSec = location.getTime() / 1000;
		weeksGPS = GPSTimeSec / GPS_SECONDS_PER_WEEK;
		secondsGPS = GPSTimeSec % GPS_SECONDS_PER_WEEK;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
 
	public float HPF(float a_GCS_z) {
		float a_HPF = 0;

		float coefficient_a = 0.9f;

		g = coefficient_a * g + (1 - coefficient_a) * a_GCS_z;

		a_HPF = a_GCS_z - g;

		return a_HPF;
	}

	// j暂时取10
	public float LPF(float[] a_HPF) {
		float sum = 0;

		for (int i = (1 - W); i <= 0; i++)
			sum += a_HPF[t + i];

		return sum / W;
	}

	private int findPeak1(float[] afterLPF, int leftDev) {
		int isPeak = 1;
		for (int i = 0; i <= N; i++) {
			if (afterLPF[t - N / 2 - leftDev] < afterLPF[t - i - leftDev])
				isPeak = 0;
			if (afterLPF[t - N / 2 - leftDev] < 0.45)
				isPeak = 0;
		}

		return isPeak;
	}

	private int ensurePP(float[] afterLPF, int leftDev) {
		float[] minus_previous = new float[j];
		float[] minus_latter = new float[j];

		for (int i = 0; i < N / 2; i++) {
			minus_previous[i] = afterLPF[t - N / 2 - leftDev]
					- afterLPF[t - N / 2 - i - 1 - leftDev];
			minus_latter[i] = afterLPF[t - N / 2 - leftDev]
					- afterLPF[t - N / 2 + i + 1 - leftDev];
		}

		if (getMax(minus_previous) > a_pp && getMax(minus_latter) > a_pp)
			return 1;
		else
			return 0;
	}

	private int ensureSlope(float[] afterLPF, int leftDev) {
		int isPeak = 1;
		// float sum_previous = 0, sum_latter=0;
		float previous = 0, latter = 0;
		for (int i = t - N; i <= t - N / 2 - 1; i++) {
			previous = afterLPF[i + 1 - leftDev] - afterLPF[i - leftDev];
			latter = afterLPF[i + N / 2 + 1 - leftDev]
					- afterLPF[i + N / 2 - leftDev];
			if (previous < 0 || latter > 0)
				isPeak = 0;
			// sum_previous += afterLPF[i+1] - afterLPF[i];
			// sum_latter += afterLPF[i+N/2+1] - afterLPF[i+N/2];
		}

		// if(sum_previous>0&&sum_latter<0)
		// return 1;
		// else
		return isPeak;
	}

	public float getMax(float[] arr) {
		float max = arr[0];
		for (int x = 1; x < arr.length; x++) {
			if (arr[x] > max)
				max = arr[x];
		}
		return max;
	}

	private float calcuDis() {
		float apk2vly = 0;
		float stepLength = 0;
		int i = 0;
		/*
		 * for(int i=0;i<=N;i++){ if((afterLPF[t-N/2]-afterLPF[t-i])>apk2vly){
		 * apk2vly=afterLPF[t-N/2]-afterLPF[t-i]; } }
		 */
		if (isFirstStep) {
			apk2vly = afterLPF[t - N / 2] - 0;
			isFirstStep = false;
		} else {
			i = findValleyAhead();

			/*
			 * while(true){
			 * 
			 * 
			 * 
			 * i++;
			 * if(afterLPF[t-N/2-i]<afterLPF[t-N/2-i-1]&&afterLPF[t-N/2-i]<afterLPF
			 * [t-N/2-i-2]&&afterLPF[t-N/2-i]<afterLPF[t-N/2-i-3])
			 * if(findPeak1(afterLPF_negative
			 * ,i)*ensurePP(afterLPF_negative,i)*ensureSlope
			 * (afterLPF_negative,i)==1) break; }
			 */
		}
		apk2vly = afterLPF[t - N / 2] - afterLPF[t - i - N / 2];
		if (apk2vly >= a_thresh)
			stepLength = (float) (1.131 * Math.log10(apk2vly) + 0.159);
		else
			stepLength = (float) (1.479 * Math.pow(apk2vly, 0.25) - 1.259);

		return stepLength;

	}

	// //磁偏角
	public float getGeoNorthDeclination() {
		// Location loc = null;
		// String providerName;
		String providerName = locManager.getBestProvider(new Criteria(), true);
		Location loc = locManager.getLastKnownLocation(providerName);

		if (loc == null
				&& locManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			// 请注意，设备要打开网络定位的选项。在室内，由于不容易搜索到GPS，建议采用network方式。
			// 否则，locManager.isProviderEnabled("network")为false，不能使用网络方式，而GPS在室内搜半天卫星都不一定有
			providerName = LocationManager.NETWORK_PROVIDER;
			loc = locManager.getLastKnownLocation(providerName);
		}
		if (loc == null)
			return 0;

		Log.d("WEI", "" + loc);

		GeomagneticField geo = new GeomagneticField((float) loc.getLatitude(),
				(float) loc.getLongitude(), (float) loc.getAltitude(),
				System.currentTimeMillis());

		float declination = geo.getDeclination();
		// return String.format("磁偏角：%7.3f", declination);
		return declination;
	}

	// private float calcuDis_a(){
	//
	// }
	public int findValleyAhead() {
		int i = 0;
		while (true) {

			i++;
			if (afterLPF[t - N / 2 - i] < afterLPF[t - N / 2 - i - 1]
					&& afterLPF[t - N / 2 - i] < afterLPF[t - N / 2 - i - 2]
					&& afterLPF[t - N / 2 - i] < afterLPF[t - N / 2 - i - 3])
				/*
				 * if(findPeak1(afterLPF_negative,i)*ensurePP(afterLPF_negative,i
				 * )*ensureSlope(afterLPF_negative,i)==1)
				 */
				break;
		}

		return i;

	}

	public String display(float[][] a) {
		String s = "";
		for (int i = 0; i < 3; i++) {

			for (int m = 0; m < 3; m++)
				s += a[i][m] + "\n";
		}
		return s;
	}

	/*
	 * public float[] getNegetive(float[] arr){ float [] arrN=new
	 * float[arr.length]; for(int i=0;i<arr.length;i++){ arrN[i]=0-arr[i]; }
	 * return arrN; }
	 */
	public double atan2(float y, float x) {
		return 2 * Math.atan(y
				/ (Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2)) + x)));
	}

	
	public float[][] reverse(float[][] a) {
		float[][] result = new float[a[0].length][a.length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				result[j][i] = a[i][j];
			}
		}
		return result;
	}

	public float[][] multiple(float[][] a, float[][] b) {
		float[][] result = new float[a.length][b[0].length];
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				for (int k = 0; k < a[0].length; k++) {
					result[i][j] = result[i][j] + a[i][k] * b[k][j];
				}
			}
		}
		return result;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sensorManager.unregisterListener(this);

		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	
	
	public void updateOreintationDisplay(int t){
		Log.d("NNNNNNNNNNNNNNNNNNNNN", "UI:"+t ); 
		text_orientation.setText("Mag:" +  hMag[t] + "\nGro:" + hGro
				+ "\nheading:" + heading[t] + "\n" + "\nheadingdiff:"
				+ (heading[t] - firstMag)); 
	}
	
	 	
		
		
		private Handler mHandler1 = new Handler() {
	        public void handleMessage (Message msg) {//此方法在ui线程运行
	            switch(msg.what) {
	            case 222:
	            	updateOreintationDisplay(msg.arg1);
	                break;

	            
	            }
	        }
	    }; 
	     
		
}
