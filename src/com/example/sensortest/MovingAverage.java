package com.example.sensortest;

import java.text.DecimalFormat;

import android.util.Log;

public class MovingAverage {
	
	float circularBuffer[];//保存传感器最近的k个数据
	public float avgv;  //返回到传感器的平均值
	public float avgv1;
	int Index;  //传感器数据数组节点位置
	int count;
	
	
	public MovingAverage(int k){
		
		circularBuffer = new float[k];
		count = 0;
		Index = 0;
		avgv = 0; 
		avgv1 = 0;
	}
	
	//获取平均值
	public float getValue(){
		
		DecimalFormat df2 = new DecimalFormat("#.00");
		avgv1 = Float.valueOf(df2.format(avgv));
		return avgv1;
	}
	
	//传递最新采集到的传感器数据
	public void pushValue(float v){
		if (count++ ==0)
		{
			primeBuffer(v);
		}
		float lastValue = circularBuffer[Index];
		avgv = avgv + (v-lastValue) / circularBuffer.length;  //计算传感器平均值
		circularBuffer[Index] = v;   //更新窗口中传感器数据
		Index = nextIndex(Index);
		
	}
	
	public long getCount ()
	{
		return count;
	}
	
	private void primeBuffer(float val) {
		// TODO Auto-generated method stub
		for(int i = 0; i < circularBuffer.length; ++i)
		{
			circularBuffer[i]=val;
		}
		avgv = val;
		
	}

	private int nextIndex(int curIndex) {
		// TODO Auto-generated method stub
		if( curIndex + 1 >= circularBuffer.length)
		{
			return 0;
		}
		return curIndex + 1;
	}
}
