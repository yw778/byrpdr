package com.docomo.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabase {// 主动测试 数据库
    private static final String name = "database";// 数据库名称
    private static final int version = 7;// 数据库版本
    private static String TbName = "ant_test";
    /**
     * 数据 id,time_index,download_ave,download_max,upload_ave,upload_max,latency,
     * network
     * */

    private static final String DB_CREATE = // /////单点测试表
    "CREATE TABLE IF NOT EXISTS " + TbName + "  (id integer primary key autoincrement, "
            + // id auto increment
            "  time_index integer,"
            + // /time
            "  download_average integer," + "  download_max integer," + "  upload_average integer,"
            + "  upload_max integer," + "  latency integer," + 
            "  upload_traffic double,"+"  download_traffic double,"+
            "  network varchar(20)," + // /network
                                                                                          // 定位为
                                                                                          // 2G，3G，Wi-Fi
            
            "  providersname varchar(20)," + // /运营商
            "  ipInformation varchar(40)," + // ip信息
            "  testmode integer, " + // /服务器测试与网站测试标志，0为服务器测试，1为第三方网站测试
            "  lat varchar(20), " + // /地理位置信息 的纬度
            "  lon varchar(20)," + // /地理位置信息的 经度
            "  floor integer" + //地理位置楼层
            ")";

    private SQLiteDatabase mSQLiteDatabase = null;
    private DBOpenHelper dbOpenHelper = null;
    private Context mycontext = null;

    // ///////////////////////////////数据库helper
    public class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context) {
            super(context, name, null, version);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.w("DBOpenHelper", "DBOpen--onCreate table is not exits");
            db.execSQL(DB_CREATE);// 主动测试表
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v("SQLiteDatabase", "SQLiteDatabase updata");
            String sql = "drop table if exists " + TbName;
            db.execSQL(sql);
            db.execSQL(DB_CREATE);
        }

    }

    // //////////////////////////////////////myTrackDatabase构造函数,取得context
    public MyDatabase(Context context) {
        this.mycontext = context;
    }

    public void open() { // ///打开数据库
        dbOpenHelper = new DBOpenHelper(mycontext);
        mSQLiteDatabase = dbOpenHelper.getWritableDatabase();
    }

    public void close() { // ///关闭数据库
        if (mSQLiteDatabase.isOpen()) {
            mSQLiteDatabase.close();
        }
    }

    /**
     * 数据 id,
     * time_index,download_ave,download_max,upload_ave,upload_max,latency,
     * network,providersname,ipInformation testmode,lat,lon 8项
     * */
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public long insertTestData(long time_index, int download_average, int download_max, int upload_average,
            int upload_max, int latency, double upload_traffic,double download_traffic,String network, String providersname, String ipInformation, int testmode,
            String lat, String lon, int floor) {
        ContentValues myini = new ContentValues();
        Cursor mycursor = selectByTime(time_index - 2, time_index + 2);
        if (mycursor == null || mycursor.getCount() < 1) {
            if (download_average <= 0 || upload_average <= 0)
                return 0L;
            myini.put("time_index", time_index);
            myini.put("download_average", download_average);
            myini.put("download_max", download_max);
            myini.put("upload_average", upload_average);
            myini.put("upload_max", upload_max);
            myini.put("latency", latency);
            myini.put("upload_traffic",upload_traffic);
            myini.put("download_traffic",download_traffic);
            myini.put("network", network);
            myini.put("providersname", providersname);
            myini.put("ipInformation", ipInformation);
            myini.put("testmode", testmode);
            if (Double.valueOf(lat) < 1 || Double.valueOf(lon) < 1) {
                lat = "not available";
                lon = "not available";
            }
            myini.put("lat", lat);
            myini.put("lon", lon);
            myini.put("floor", floor);
            Log.w("insertTestData", "insert succeed");
            return mSQLiteDatabase.insert(TbName, null, myini);

        } else {
            Log.w("insertTestData", "insert unsucceed");
            return 0;
        }
        // //
    }

    public boolean deleteTable() {
        String sql = "drop table if exists " + TbName;
        mSQLiteDatabase.execSQL(sql);
        mSQLiteDatabase.execSQL(DB_CREATE);
        return false;
    }

    public Cursor fetchAllData() {
        Cursor mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                "download_average", "latency","upload_traffic","download_traffic", "network", "providersname", "ipInformation", "testmode", "lat", "lon","floor" },
                null, null, null, null, "time_index DESC");
        mcursor.moveToFirst();
        Log.v("fetchDataBynetwork---", "cursor number=" + mcursor.getCount() + "");
        return mcursor;
    }

    public Cursor fetchDetailAllData() {
        Cursor mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                "upload_max", "download_average", "download_max", "latency", "upload_traffic", "download_traffic", "network", "providersname",
                "ipInformation", "testmode", "lat", "lon", "floor" }, null, null, null, null, "time_index DESC");
        mcursor.moveToFirst();
        Log.v("fetchDataBynetwork---", "cursor number=" + mcursor.getCount() + "");
        return mcursor;
    }

    public Cursor selectDataById(int id) {
        Cursor mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat", "lon","floor"},
                "id=" + id, null, null, null, "time_index DESC");
        mcursor.moveToFirst();
        Log.v("fetchDataBynetwork---", "cursor number=" + mcursor.getCount() + "");
        return mcursor;
    }

    public boolean deleteById(int id) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete(TbName, "id=?", new String[] { id + "" });
        // db.close();
        Log.v("fetchDataBynetwork---", " database is trying to delete id=" + id);
        return false;

    }

    public Cursor fetchDataByTime(boolean flag) {// /false为 降序//true 为升序
        Cursor mcursor;
        if (flag == false) {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "time_index DESC");
        } else {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "time_index ASC");
        }
        return mcursor;
    }

    public Cursor fetchDataByNetwork(String network) {// /0为2g 1为3g,
                                                      // 2为Wi-Fi////这里和数据库设计略有不同，一个是int型
                                                      // 一个是boolean型
        if (network.equals("2G") || network.equals("3G") || network.equals("Wi-Fi")) {
            Log.w("fetchDataBynetwork---", "cursor number network=" + network);
            Cursor mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, " network like ?", new String[] { "" + network }, null, null, "time_index DESC");

            Log.w("fetchDataBynetwork---", "cursor number=" + mcursor.getCount() + "");
            return mcursor;
        } else {
            Log.w("fetchDataBynetwork---", "cursor number=0");
            return null;
        }
    }

    public Cursor fetchDataByUploadSpeed(boolean flag) {// /上行数据 0为降序 1为升序
        Cursor mcursor;
        if (flag == false) {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "upload_average DESC");
        } else {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "upload_average  ASC");

        }
        mcursor.moveToFirst();
        return mcursor;
    }

    /**
     * public Cursor fetchDataByDownloadSpeed(boolean flag) flag 下行数据 0为降序 1为升序
     * */
    public Cursor fetchDataByDownloadSpeed(boolean flag) {
        Cursor mcursor;
        if (flag == false) {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "download_average DESC");
        } else {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "download_average ASC");
        }
        mcursor.moveToFirst();
        return mcursor;
    }

    public Cursor fetchDataByLatency(boolean flag) {// /下行数据 0为降序 1为升序
        Cursor mcursor;
        if (flag == false) {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "latency DESC");
        } else {
            mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                    "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat",
                    "lon","floor" }, null, null, null, null, "latency ASC");

        }
        mcursor.moveToFirst();
        return mcursor;
    }

    public Cursor selectByTime(long startTime, long endTime) {// /下行数据 0为降序 1为升序
        Cursor mcursor;
        mcursor = mSQLiteDatabase.query(TbName, new String[] { "id as _id", "time_index", "upload_average",
                "download_average", "latency", "upload_traffic", "download_traffic", "network", "providersname", "ipInformation", "testmode", "lat", "lon","floor" },
                " time_index <= " + endTime + " and time_index >= " + startTime, null, null, null, null);
        mcursor.moveToFirst();
        return mcursor;
    }

}
