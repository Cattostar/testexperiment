package com.example.starry.testservice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TimePicker;
import android.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class diary_activity extends Activity implements View.OnClickListener {//store the number


    private final String dirName = "ActivityRecorder";// 目录名
    private final String timeFileName = "ActivityTime.txt";// 活动时间文件名
    private final String typeFileName = "ActivityType.txt";// 活动类型文件名
    private Spinner spinnerActivity;
    private Button btnAddActivity;
    private Button btnStart;
    private Button btncheck;
    //private TextView lblStatus;
    private static ArrayList<String> arrActivity = null;
    private FileUtils fileUtils;
    private String curActivity = null;// 当前活动
    private RatingBar ratingBar = null;
    private TextView ratingValue;
    private int curHour,curMin;//save the last end time
    private TimePicker timePicker;
    private int startHour, startMinute, endHour, endMinute;
    private TimePickerDialog timePickerDialog;
    private Button startTimePicker, endTimePicker;
    private TextView textViewSrt, textViewEnd;
    private boolean check1,check2,check3;
    private String value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        init();
    }

    private void init() {
        try {
            fileUtils = new FileUtils(dirName, timeFileName, typeFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        spinnerActivity = (Spinner) findViewById(R.id.spinner_activity);
        btnAddActivity = (Button) findViewById(R.id.button_activity_add);
        ratingValue = (TextView) findViewById(R.id.rating_val);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        btnStart = (Button) findViewById(R.id.button_start);
        btncheck=(Button)findViewById(R.id.button5);
        fillSpinner();
        btnAddActivity.setOnClickListener(new Add_ButtonClick());
        btnStart.setOnClickListener(new Save_ButtonClick());
        startTimePicker = (Button) findViewById(R.id.button_srt);
        endTimePicker = (Button) findViewById(R.id.button_end);
        textViewSrt = (TextView) findViewById(R.id.textView6);
        textViewEnd = (TextView) findViewById(R.id.textView5);
        startTimePicker.setOnClickListener(this);
        endTimePicker.setOnClickListener(this);
        btncheck.setOnClickListener(this);


        // 初使化
        curActivity = fileUtils.GetCurrentActivityName();
        if (curActivity != null && !curActivity.equals("")) {
            int index = arrActivity.indexOf(curActivity);
            if (index != -1) {
                spinnerActivity.setSelection(index);// 设置默认值
                btnStart.setText(R.string.end);
                //lblStatus.setText(curActivity + getString(R.string.ing));
            }
        }
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingValue.setText(Float.toString(rating));//0.5个步长
                //Toast.makeText(diary_activity.this, String.format("current rating val:%f, fromUser = %d", rating, fromUser ? 1:0),
                //Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void fillSpinner() {
        arrActivity = fileUtils.getAllActivityType();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arrActivity);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        spinnerActivity.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {//choose time
        switch (view.getId()) {
            case R.id.button_srt:
                TimePickerDialog time_start = new TimePickerDialog(diary_activity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // TODO Auto-generated method stub
                        Toast.makeText(diary_activity.this, hourOfDay + "hour " + minute + "minute", Toast.LENGTH_SHORT).show();
                            startHour=hourOfDay;
                            startMinute=minute;
                        textViewSrt.setText(startHour+"时"+startMinute+"分");
                        textViewEnd.setText(endHour+"时"+endMinute+"分");
                    }
                }, curHour, curMin, true);//each time storage
                time_start.show();
               // curHour=startHour;
               // curMin=startMinute;
                break;

            case R.id.button_end:
                TimePickerDialog time_end = new TimePickerDialog(diary_activity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // TODO Auto-generated method stub
                        Toast.makeText(diary_activity.this, hourOfDay + "hour " + minute + "minute", Toast.LENGTH_SHORT).show();
                        endHour=hourOfDay;
                        endMinute=minute;
                        textViewSrt.setText(startHour+"时"+startMinute+"分");
                        textViewEnd.setText(endHour+"时"+endMinute+"分");
                    }
                }, curHour, curMin, true);//each time storage
                time_end.show();
                //curHour=endHour;
                //curMin=endMinute;
                break;

            case R.id.button5:
                Intent intent2 = new Intent(diary_activity.this,check_activity.class);
                startActivity(intent2);
                break;
        }
    }


    class Save_ButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {//if no activity,get time
            if (arrActivity == null || arrActivity.size() == 0) {//about activity type
                new AlertDialog.Builder(diary_activity.this)
                        .setTitle(R.string.hint_no_activity)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(R.string.ok, null).show();
                check1=false;
                return;
            }else{
                curActivity = spinnerActivity.getSelectedItem().toString();
                check1 = true;
            }
            //with activity type
            //judge time picker
            if (ratingBar.getRating() == 0)//如果得到的数是零
            {
                Toast.makeText(diary_activity.this, "请填写疲劳分数！",
                        Toast.LENGTH_SHORT).show();
                check2=false;
                return;
            }else{
                value = ratingValue.getText().toString();
                check2=true;
            }
            // 当前没有活动在执行，则开始
            //start time
            //end time
            //make json object
           if(endHour*60+endMinute>startHour*60+startMinute){
               check3=true;
           }else{
               Toast.makeText(diary_activity.this, "时间选择有误！",
                       Toast.LENGTH_SHORT).show();
               check3=false;
           }
           if(check1 && check2 && check3){
                SaveData();
                startHour=endHour;
                startMinute=endMinute;
           }else{
                //notification

           }
           //SaveData(JSONObject);

        }

    }

    // 保存数据
    private void SaveData(){
        //分成start和stop讨论
        //String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        // String activity= curActivity;
        JSONObject msg = new JSONObject();
        try {
            msg.put("事件",curActivity);
            msg.put("开始时间", startHour+":"+startMinute);
            msg.put("结束时间", endHour+":"+endMinute);
            msg.put("疲劳程度", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*String msg1 = curActivity + "," + (startHour*60+startMinute) +  "," + value;
        String msg2 = curActivity + "," +  (endHour*60+endMinute) + "," + value;*/
        try {
            fileUtils.appendLine(msg);//自己写的函数
            //fileUtils.appendLine(msg2);//自己写的函数
            ratingBar.setRating(0);//当开始的时候把rating设置为0
            curActivity = null;
            textViewSrt.setText(endHour+"小时"+endMinute+"分");//set end hour
            textViewEnd.setText(endHour+"小时"+endMinute+"分");
            curHour=endHour;
            curMin=endMinute;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(diary_activity.this, String.format("出错"), Toast.LENGTH_SHORT).show();
        }
    }


    class Add_ButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            LayoutInflater factory = LayoutInflater.from(diary_activity.this);// 提示框
            final View view = factory.inflate(R.layout.activity_add, null);
            final EditText edit = (EditText) view.findViewById(R.id.edit_activity_name_add);// 获得输入框对象

            new AlertDialog.Builder(diary_activity.this)
                    .setTitle(R.string.input_activity_name) // 提示框标题
                    .setView(view)
                    .setPositiveButton(
                            R.string.ok,// 提示框的两个按钮
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { // 事件
                                    String newActivityName = edit.getText()
                                            .toString();
                                    newActivityName = newActivityName.trim();
                                    if (newActivityName.equals("")) {
                                        return;// 输入为空时不保存
                                    }
                                    boolean res = fileUtils.addActivityType(newActivityName);
                                    if (res) {
                                        fillSpinner();
                                    }
                                    int index = arrActivity.indexOf(newActivityName);
                                    if (index != -1) {
                                        spinnerActivity.setSelection(index);// 设置默认选择添加项
                                    }
                                }
                            }).setNegativeButton(R.string.cancel, null)
                    .create().show();
        }
    }
}





