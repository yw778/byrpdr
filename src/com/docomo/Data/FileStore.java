package com.docomo.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.json.JSONObject;

import edu.bupt.testinfo.UploadData;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * testinfo 表 cell 表： wifi 表： gps_wifi 表： cell_scan 表： 后缀.dat
 * */
public class FileStore {
    private final String TAG = "FileStore";

    public final static String tablename_testinfo = "testinfo";
    public final static String tablename_cell = "cell";
    public final static String tablename_cell_scan = "cell_scan";
    public final static String tablename_wifi = "wifi";
    public final static String tablename_gps_wifi = "gps_wifi";
    private final static String[] tablename = new String[] { tablename_testinfo, tablename_cell, tablename_cell_scan,
            tablename_wifi, tablename_gps_wifi };

    private final static String localdata = "localdata";

    private final static String testinfoURL = "http://xugang.host033.youdnser.com/dataServer/insert_testinfo_db_test.php";
    private final static String cellURL = "http://xugang.host033.youdnser.com/dataServer/insert_cell_db.php";
    private final static String wifiURL = "http://xugang.host033.youdnser.com/dataServer/insert_wifi_db.php";
    private final static String gps_wifiURL = "http://xugang.host033.youdnser.com/dataServer/insert_gps_wifi_db.php";
    private final static String cell_scanURL = "http://xugang.host033.youdnser.com/dataServer/insert_cell_scan_db.php";
    private final static String[] urlArray = new String[] { testinfoURL, cellURL, cell_scanURL, wifiURL, gps_wifiURL };

    private String foldername = "/sdcard/ANTDATA";
    /**
     * private int testinfo_uploadnumber=0; private int cell_uploadnumber=0;
     * private int cell_scan_uploadnumber=0; private int wifi_uploadnumber=0;
     * private int gps_wifi_uploadnumber=0;
     * */
    private int[] uploadnumber = new int[5];// 已上传的数目
    private Context mcontext;
    private int uploadOverTag = 0;

    /**
     * 构造函数，检查目录并创建目录
     * */
    public FileStore(Context mcontext) {
        this.mcontext = mcontext;
        boolean sdCardExit = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExit)
            foldername = "/sdcard/ANTDATA";
        else
            foldername = this.mcontext.getFilesDir() + "/ANTDATA";
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
            file.mkdirs();
            // Log.v("","创建文件="+file.toString());
        }
        // Log.v("","文件存在="+file.toString());
    }

    private void recreateFile(String f) {
        File file = new File(foldername + "/" + f + ".dat");
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
        File file = new File(foldername + "/" + localdata + ".dat");
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
        if (!(filename.equals(FileStore.tablename_cell) || filename.equals(FileStore.tablename_cell_scan)
                || filename.equals(FileStore.tablename_gps_wifi) || filename.equals(FileStore.tablename_testinfo)
                || filename.equals(FileStore.tablename_wifi) || filename.equals(FileStore.localdata))) {
            Log.w(TAG, TAG + "Store name is erro");
            return false;
        }
        storeOneline(filename, storecontent, true);
        return true;
    }

    /**
     * storeOneline(String filename,String storecontent,boolean isContinueWrite)
     * 
     * */
    private boolean storeOneline(String filename, String storecontent, boolean isContinueWrite) {
        FileOutputStream fw = null;
        try {
            fw = new FileOutputStream(new File(foldername + "/" + filename + ".dat"), isContinueWrite);
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

    private void readLineNumber() {
        String filename = foldername + "/" + localdata + ".dat";
        File file = new File(filename);
        FileReader fr;// 读取器
        BufferedReader br;//
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String temp = "";
            for (int i = 0; i < 5; i++) {
                temp = br.readLine();
                if (temp == null)
                    return;
                // Log.w("~~~temp","read uploadnumber["+i+"]="+temp.trim());
                uploadnumber[i] = Integer.valueOf(temp.trim());
            }
            fr.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean Store(String filename, JSONObject storecontent) {
        return this.Store(filename, storecontent.toString());
    }

    /**
     * 上传函数，根据返回值，清除本地记录 功能：尽量上传数值
     * 
     * @throws
     * */
    public boolean uploadStore() {
        /*
         * 获取已读取的行数
         */
        // File file=new File(foldername+"/"+"readnumber"+".dat");

        /**
         * private int testinfo_uploadnumber=0; private int cell_uploadnumber=0;
         * private int cell_scan_uploadnumber=0; private int
         * wifi_uploadnumber=0; private int gps_wifi_uploadnumber=0;
         * */
        // 读取各个行数
        // uploadnumber[0] = preferences.getInt(tablename_testinfo, 0);
        // uploadnumber[1] = preferences.getInt(tablename_cell, 0);
        // uploadnumber[2] = preferences.getInt(tablename_cell_scan, 0);
        // uploadnumber[3] = preferences.getInt(tablename_wifi, 0);
        // uploadnumber[4] = preferences.getInt(tablename_gps_wifi, 0);
        readLineNumber();
        uploadOverTag = 5;
        for (int i = 0; i < 5; i++) {
            uploadLineInfo t = new uploadLineInfo(urlArray[i], i);
            t.start();
        }

        return true;
    }

    /**
     * 类说明：将文件 filename中的未传输的数据传输到相对应的服务器url中 未传输的为第uploadnumber[n]行数据
     * */
    class uploadLineInfo extends Thread {
        int line = 0;
        int totalline = 0;
        int tagnumber = 0;
        String filename = "";
        String url = "";

        uploadLineInfo(String url, int n) {
            this.line = uploadnumber[n];
            this.filename = foldername + "/" + tablename[n] + ".dat";
            this.url = url;
            this.tagnumber = n;
        }

        public void run() {
            try {
                totalline = getTotalLines(new File(filename));
                // Log.v(TAG,TAG+"totalline ="+totalline+" line="+line);
            } catch (IOException e) {
                Log.d(TAG, TAG + "totalline reading erro");
                e.printStackTrace();
            }
            if (totalline < 1)
                return;
            if (line < 1 || line > (totalline + 1)) {
                // line表示应该读取的行数， 如果他出错，说明文件被删除过，则从第一行开始重新上传
                line = 1;
                uploadnumber[tagnumber] = 1;
                Log.v(TAG, TAG + "line is out of bounder");

            }
            File file = new File(this.filename);
            FileReader fr;// 读取器
            BufferedReader br;//
            try {
                fr = new FileReader(file);
                br = new BufferedReader(fr);
                for (int i = 1; i < line; i++) {
                    String l = br.readLine();
                    // Log.v("for","for"+i+" "+l);
                }
                for (int j = line; j <= totalline; j++) {
                    line++;
                    String l = br.readLine();
                    // Log.v("uploading",tablename[tagnumber]+"uploading ...."+j+"  "+l);
                    String result = uploadToServer(this.url, l);
                    // Log.v("uploading",tablename[tagnumber]+"uploading ...."+j+"  result="+result);

                }
                /**
                 * 写入信息
                 * */
                // Log.v("uploading","the uploadnumber["+tagnumber+"]="+uploadnumber[tagnumber]);
                uploadnumber[tagnumber] = line;

                fr.close();
                br.close();
                uploadOverTag--;// 如果减到0，说明5个线程都运行完毕，则更新local文件
                if (uploadOverTag == 0) {
                    // Log.w("~~~localdata","Thread is over");
                    /**
                     * 写入信息，更新已上传的行数
                     * */
                    storeOneline(localdata, uploadnumber[0] + "", false);
                    storeOneline(localdata, uploadnumber[1] + "", true);
                    storeOneline(localdata, uploadnumber[2] + "", true);
                    storeOneline(localdata, uploadnumber[3] + "", true);
                    storeOneline(localdata, uploadnumber[4] + "", true);

                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    // 文件内容的总行数。
    private static int getTotalLines(File file) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }

    private String uploadToServer(String serverurl, String json) {
        UploadData upload_info_result = new UploadData(serverurl, json);
        String re = upload_info_result.upData();
        return re;
    }

}
