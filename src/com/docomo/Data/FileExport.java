package com.docomo.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FileExport {
    private final String TAG = "FileExport";

    public final static String tablename_network = "network";
    public final static String tablename_time = "time";
    public final static String tablename_up_speed= "upload_speed";
    public final static String tablename_down_speed = "download_speed";
    public final static String tablename_time_delay = "time_delay";
    
    private final static String[] tablename = new String[] { tablename_network, tablename_time, tablename_up_speed, tablename_down_speed, tablename_time_delay };

    private final static String localdata = "localdata";

    private String foldername = "/sdcard/DataExport";
    /**
     * private int testinfo_uploadnumber=0; private int cell_uploadnumber=0;
     * private int cell_scan_uploadnumber=0; private int wifi_uploadnumber=0;
     * private int gps_wifi_uploadnumber=0;
     * */
  //  private int[] uploadnumber = new int[5];// 已上传的数目
    
    
    private Context mcontext;
   
    /**
     * 构造函数，检查目录并创建目录
     * */
    
    public FileExport(Context mcontext) {
        this.mcontext = mcontext;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist)
            foldername = "/sdcard/DATAEXPORT";
        else
            foldername = this.mcontext.getFilesDir() + "/DATAEXPORT";
        // Log.v(TAG,TAG+"sdcard file path="+foldername);
        File file = new File(foldername);
        recreateFolder(file);
        recreateLocalDataFile();
        for (int i = 0; i < 5; i++) {
            recreateFile(tablename[i]);
        }

    }

    private void recreateFolder(File file) {
        if (!file.exists()) {
            file.mkdirs();//在不存在的目录中创建文件夹
            // Log.v("","创建文件="+file.toString());
        }
        // Log.v("","文件存在="+file.toString());
    }

    private void recreateFile(String f) {
        File file = new File(foldername + "/" + f + ".txt");
        if (!file.exists())
            try {
                file.createNewFile();
                Store(f, "storecontent");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
    }

    private void recreateLocalDataFile() {
        File file = new File(foldername + "/" + localdata + ".txt");
        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
    }

    /**
     * 数据存储 （要保证实时存储）
     * */
    public boolean Store(String filename, String storecontent) {
        if (filename == null || filename.equals("") || storecontent == null)
            return false;
        if (!(filename.equals(FileExport.tablename_network) || filename.equals(FileExport.tablename_time)
                || filename.equals(FileExport.tablename_up_speed) || filename.equals(FileExport.tablename_down_speed)
                || filename.equals(FileExport.tablename_time_delay) || filename.equals(FileExport.localdata))) {
            Log.w(TAG, TAG + "Store name is erro");
            return false;
        }
      
        return true;
    }

    /**
     * storeOneline(String filename,String storecontent,boolean isContinueWrite)
     * 
     * */
    public boolean storeOnline(String filename, String storecontent, boolean isContinueWrite) {
        
    	FileOutputStream fw = null;
        
    	Toast.makeText(mcontext, "测试数据已导出", Toast.LENGTH_SHORT).show();
        try {
            fw = new FileOutputStream(new File(foldername + "/" + filename + ".txt"), isContinueWrite);
        } catch (IOException e) {
            Log.w(TAG, TAG + "FileWriter  is erro");
            e.printStackTrace();
        }
        try {
            fw.write((storecontent + "\t\n").getBytes());
            fw.flush();
        } catch (IOException e) {
            Log.w(TAG, TAG + "FileWriter.write  is erro");
            e.printStackTrace();
        }
        try {
            fw.close();
        } catch (IOException e) {
            Log.w(TAG, TAG + "FileWriter.close  is erro");
            e.printStackTrace();
        }
        return true;
    
    }
   
}

