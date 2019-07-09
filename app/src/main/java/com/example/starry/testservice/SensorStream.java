package com.example.starry.testservice;


import android.os.Handler;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.stream.Stream;

public class SensorStream {

    private float[] readingElement;
    private BufferedWriter mWriter;
    private int fullsize=1000;
    private ArrayList mList = new ArrayList(1000);//capacity
    ArrayList copyMList = new ArrayList(1000); //copy one
    private Stream mStream;
    private int cursor = 0;
    private boolean isFullFlag = false;




    public synchronized ArrayList getmList() {
        return mList;
    }

    public BufferedWriter getmWriter() {
        return mWriter;
    }

    public int getCursor() {
        return cursor;
    }

    public boolean isFullFlag() {
        return isFullFlag;
    }

    public float[] getReadingElement() {
        return readingElement;
    }

    public ArrayList getCopyMList() {
        return copyMList;
    }

    public void setFullsize(int fullsize) {
        this.fullsize = fullsize;
    }

    public void setFullFlag(){

        if (isFullFlag)
        {
            isFullFlag=false;
        }
    }

    public void setmList(int size){

        mList=new ArrayList(size);
        copyMList = new ArrayList(size);
    }


    public synchronized void updataStream(JSONArray data) {

        if (this.cursor < fullsize) {
            //小的时候
            mList.size();
            this.mList.add(data);//用arraylist存起来
            this.cursor++; //加一个
            if(cursor ==fullsize){
                isFullFlag=true;
                cursor=0;
            }
        }

    }


}

