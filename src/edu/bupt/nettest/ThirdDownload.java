package edu.bupt.nettest;

import java.text.SimpleDateFormat;
import java.util.Date;

//import edu.bupt.anttest.WebTestFragment;
import edu.bupt.unotest.UNOTest;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ThirdDownload {
  
	public native int  ThirdDownloadstartFromJNI();   
    public native void ThirdDownloadstopFromJNI();  
    public native int  ThirdDownloadgetAveSpeed();
    public native int  ThirdDownloadgetTestState();


    public static final String Tag = "TestResult";
	
    public static final String Service_Data = "edu.bupt.anttest";
    
    public native long  ThirdDownloadFirstByteTimeFromJNI();
    public native long  ThirdDownloadConnectTimeFromJNI();
    public native long  ThirdDownloadDownloadTimeFromJNI();
    public native long  ThirdDownloadOpenTimeFromJNI();
    public native long  ThirdDownloadParseTimeFromJNI();
    
    public native String  ThirdDownloadIPAddrFromJNI();
    private Message msg = new Message(); 
    
    private UNOTest unoTest;
    private String date;
    Context mcontext;
    private boolean autoUpload = true;
    
    public native void  ThirdDownloadsetThreadNum(int num);  
    private native void  ThirdDownloadsetServer(String serverAddress);
    public native void  ThirdDownloadsetDuration(int duration);  
    
    public static final int STATE_READY = 0x0; // states of test
    public static final int STATE_STARTED = 0x1; //
                                                 // Modified by x7, Mar 15, 2013
    public static final int STATE_RUNNING = 0x2;
    public static final int STATE_COMPLETE = 0x3;
    public static final int STATE_KILLED = 0x4;
    
    long firstbytetime=0;
    long connecttime=0;
    long downloadtime=0; 
    long DNStime = 0;
    long opentime = 0;
    String IPAddr;  
    double qualityscore = 0.0D;
    int downspeed = 0;
    
    private int state = ThirdDownload.STATE_READY;
    
    private String serverAddress = "www.sina.com.cn";
    public ThirdDownload(Context context){
    	mcontext = context;
    	this.ThirdDownloadsetServer(serverAddress);
    }
    public ThirdDownload(UNOTest unoTest){
    	this.unoTest = unoTest;
    	this.ThirdDownloadsetServer(serverAddress);
    }
    
    public int getState(){
    	return state;
    }
    
    public boolean setTestServer(String serverAddress){
    	this.serverAddress = serverAddress;
    	this.ThirdDownloadsetServer(serverAddress);
    	return true;
    }

    static {
        System.loadLibrary("nettest");
    }
    
    public void start(){
    	state = ThirdDownload.STATE_RUNNING; // set state
    	SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        date = formatter.format(curDate);
        new Thirddownloadjni().start();	
        
		
    }
    
    class Thirddownloadjni extends Thread{
    	public void run(){  
    		ThirdDownloadstartFromJNI();
    		state = ThirdDownload.STATE_COMPLETE;
//    		gettotaltime();
    	}
    }
    Handler mhandler = new Handler(){};
    Runnable a = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
    	
    };
    public long getFirstByteTime(){
   	 
   	 return ThirdDownloadFirstByteTimeFromJNI();
    }
   public long getDownloadTime(){
   	 
   	 return ThirdDownloadDownloadTimeFromJNI();
    }
   public long getConnectTime(){ 
	 
	 return ThirdDownloadConnectTimeFromJNI();
   }
   
   public String getIPAddr(){
   	
   	return ThirdDownloadIPAddrFromJNI();
   }
   public long getDNSTime(){
	   
	   return ThirdDownloadParseTimeFromJNI();
   }
   public long getOpenTime(){
	   
	   return ThirdDownloadOpenTimeFromJNI();
   }
   public int downloadspeed(){
	   
	   return ThirdDownloadgetAveSpeed();
   }
   
   public void sendToActivity(String str) {
       Intent i = new Intent(Service_Data);
       i.putExtra("ServiceState", str);
       mcontext.sendBroadcast(i);
   }
//   public void gettotaltime(){
//	   	IPAddr = getIPAddr();
//	   	WebTestFragment.IpAdrress = IPAddr;
//	   	
//	   	DNStime = getDNSTime();
//		Log.d(Tag, "the DNStime is "+DNStime);
//		WebTestFragment.DNStime = (int)((DNStime/1000 == 0?0:DNStime/1000));
//	   	
//	   	connecttime=getConnectTime();
//		Log.d(Tag, "the connecttime is "+connecttime);
//		WebTestFragment.connecTtime = (int)((connecttime/1000 == 0?0:connecttime/1000));
//	   	
//		firstbytetime=getFirstByteTime();
//		Log.d(Tag, "the firstbytetime is "+firstbytetime);
//		WebTestFragment.firstByteTime = (int)((firstbytetime/1000 == 0?0:firstbytetime/1000));
//		
//		downloadtime=getDownloadTime(); 
//		Log.d(Tag, "the downloadtime is "+downloadtime);
//		WebTestFragment.downloadTime = (int)((downloadtime/1000 == 0?0:downloadtime/1000));
//		
//		opentime = getOpenTime();
//		Log.d(Tag, "the open time is "+opentime);
//		WebTestFragment.opentime = (int)((opentime/1000 == 0?0:opentime/1000));
//		
//		downspeed = downloadspeed();
//		Log.d(Tag, "the downspeed is "+downspeed);
//		WebTestFragment.avespeed = downspeed;
//		
//		qualityscore = calcMeanUserQuality(100.0D,DNStime,connecttime,firstbytetime,downloadtime,downspeed*8*1024);
//		qualityscore = (double)(Math.round(qualityscore*10)/10);
//		WebTestFragment.qualityscore = qualityscore;
//		
//		
//		WebTestFragment.handler.sendEmptyMessage(0);
//		
//	}
    public boolean setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
        return true;
    }
    
    private double calcMeanUserQuality(double paramDouble, long paramLong1, long paramLong2, long paramLong3, long paramLong4, long paramLong5)
	  {
	    if ((paramDouble < 0.0D) || (paramLong1 < 0L) || (paramLong2 < 0L) || (paramLong3 < 0L) || (paramLong4 < 0L) || (paramLong5 < 0L))
	      return -1.0D;
	    long l1 = (paramLong2 + paramLong3) / 1000L;
	    double d1 = 0.0;
	    double d2 = 0.0;
	    long l2 = 0;
	    double d3 = 0.0;
		double d4 = 0.0;
	    long l3 = 0;
	    double d5 = 0.0;
		double d6 = 0.0;
	    double d7 = 0.0;
	    double d8 = 0.0;
	    if (l1 > 0L)
	    {
	      d1 = getd1(l1);
	      d2 = LimitRange(d1, 0.0D, 100.0D);
	      l2 = (paramLong4 + (paramLong1 + paramLong2)) / 1000L;
	      if (l2 >= 0L){
	        d3 = getd3(l2);
	      }else{
	    	  d3 = 0.0D;
	      }
	      d4 = LimitRange(d3, 0.0D, 100.0D);
	      l3 = paramLong5 / 1024L;
	      if (l3 >= 0L){
	        d5 = getd5(l3);
	      }else{
	    	  d5 = 0.0D;
	      }
	      d6 = LimitRange(d5, 0.0D, 100.0D);
	      if (paramDouble >= 97.0D)
	        d7 = getd7(paramDouble);
		  else d7 = 20.0D * paramDouble / 97.0D;
		  d8 = LimitRange(d7, 0.0D, 100.0D);
	    }
		return 0.2D * d2 + 0.3D * d4 + 0.3D * d6 + 0.2D * d8;
		}
	private double getd1(long l1){
		double d1 = 0.0;
		if (l1 < 140L)
	      {
	        d1 = 80.0D + 20.0D * (140L - l1) / 140.0D;
	        return d1;
	      }
	      if (l1 < 195L)
	      {
	        d1 = 60.0D + 20.0D * (195L - l1) / 55.0D;
	        return d1;
	      }
	      if (l1 < 270L)
	      {
	        d1 = 40.0D + 20.0D * (270L - l1) / 75.0D;
	        return d1;
	      }
	      if (l1 < 500L)
	      {
	        d1 = 20.0D + 20.0D * (500L - l1) / 230.0D;
	        return d1;
	      }
	      if (l1 < 60000L)
	      {
	        d1 = 0.0D + 20.0D * (60000L - l1) / 60000.0D;
	        return d1;
	      }
	      d1 = 0.0D;
	        return d1;
		}
	private double getd3(long l2){
		double d3 = 0.0;
		if (l2 < 2000L)
	      {
	        d3 = 80.0D + 20.0D * (2000L - l2) / 2000.0D;
	        return d3;
	      }
	      if (l2 < 6500L)
	      {
	        d3 = 60.0D + 20.0D * (6500L - l2) / 4500.0D;
	        return d3;
	      }
	      if (l2 < 11500L)
	      {
	        d3 = 40.0D + 20.0D * (11500L - l2) / 5000.0D;
	        return d3;
	      }
	      if (l2 < 21000L)
	      {
	        d3 = 20.0D + 20.0D * (21000L - l2) / 9500.0D;
	        return d3;
	      }
	      if (l2 < 60000L)
	      {
	        d3 = 0.0D + 20.0D * (60000L - l2) / 60000.0D;
	        return d3;
	      }
	      d3 = 0.0D;
	        return d3;
		}
	private double getd5(long l3){
		double d5 = 0.0;
		if (l3 < 40L)
	      {
	        d5 = 0.0D + l3 / 40.0D;
	        return d5;
	      }
	      if (l3 < 80L)
	      {
	        d5 = 20.0D + (l3 - 40L) / 40.0D;
	        return d5;
			}
	      if (l3 < 120L)
	      {
	        d5 = 40.0D + (l3 - 80L) / 40.0D;
	        return d5;
	      }
	      if (l3 < 160L)
	      {
	        d5 = 60.0D + (l3 - 120L) / 40.0D;
	        return d5;
	      }
	      if (l3 < 300L)
	      {
	        d5 = 80.0D + 20.0D * (l3 - 160L) / 140.0D;
	        return d5;
	      }
	      d5 = 100.0D;
	        return d5;
		}
	private double getd7(double paramDouble){
		double l4 = 0.0;
		if (paramDouble < 98.0D){
			l4 = 20.0D + 20.0D * (98.0D - paramDouble);
			return l4;
			}
		else if (paramDouble < 99.0D){
			l4 = 40.0D + 20.0D * (99.0D - paramDouble);
			return l4;
			}
		else if (paramDouble < 99.900000000000006D){
		    l4 = 60.0D + 20.0D * (99.900000000000006D - paramDouble) / 0.9D;
			return l4;
			}
		else{
		    l4 = 80.0D + 20.0D * (100.0D - paramDouble) / 0.01D;
			return l4;
			}
  }
	  
	private static double LimitRange(double paramDouble1, double paramDouble2, double paramDouble3)
	  {
	    if (paramDouble1 < paramDouble2)
	      return paramDouble2;
	    if (paramDouble1 > paramDouble3)
	      return paramDouble3;
	    return paramDouble1;
	  }    
    
}
