//LocationInfo.java
//created unknown
//modified by x7, May 28, 2013

package edu.bupt.testinfo;

import com.example.sensortest.GraphActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationInfo {
    Context context;

    //gaode
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private int currentfloor = 0;

    public LocationInfo(Context context) {
        this.context = context;
        initGaodeLocationClient();
        Log.d("locationinfo", "initial");
    }

    /**
     *gaode 
     */
    private void initGaodeLocationClient() {
    	currentLatitude = GraphActivity.rPointX;
    	currentLongitude = GraphActivity.rPointY;
    	currentfloor = GraphActivity.floor;
    }

    public String getProviderName() {// bd09ll等信息
        return "gaode";
    }
    
    public String getBaiDuCoorType() {// /GPS+radius Network+radius
//        if (BaiDuoption != null)
//            return BaiDuoption.getCoorType();
//        else
            return "UNKNOWN";

    }

    public double getBDLatitude() {
        return currentLatitude;
    }

    public double getBDLongitude() {
        return currentLongitude;
    }
    
    public int getFloor() {
    	return currentfloor;
    }
}
