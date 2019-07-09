package com.example.starry.testservice;


import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.support.constraint.Constraints.TAG;

public class MainActivity extends Activity {
    private AccelerometerService s;
    private boolean mIsBound=false;
    private String curID;
    private String curTime;


    private String filename = null;//要修改
    private String myContext1;
    private Vibrator vibrator;
    private TimerFrame mTimeTextView;

    private Thread mainThread;

    private int index =0;
    private android.os.Handler mHandler = new android.os.Handler();
    private String time;
    private String pace;
    private EditText idd;
    private static float time_tag = 0;

    private PowerManager.WakeLock wakeLock = null;

    static boolean record;

    private SensorStream newStream = new SensorStream();
    private boolean threadFlag=false;
    private Thread mThread = null;
    private Runnable textFileLogger;
    private MediaPlayer mediaPlayer;
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;


    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            s = ((AccelerometerService.AccBinder) binder).getService();
            //Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
                    //.show();
        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
            //Toast.makeText(MainActivity.this, "Disconnected",
                    //Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnStart = (Button) findViewById(R.id.btn_start);
        final Button btnStop = (Button) findViewById(R.id.btn_stop);
        final Button btnShow = (Button) findViewById(R.id.btn_show_data);
        final TextView accData = (TextView) findViewById(R.id.acc_text_data);
        //mTimeTextView = (TimerFrame) findViewById(R.id.electricity_countdown);
        final TextView instruction1= (TextView) findViewById(R.id.instruction);
        final TextView instruction2= (TextView) findViewById(R.id.instruction2);
        final TextView instruction3= (TextView) findViewById(R.id.instruction3);
        final Spinner  paceChoice= (Spinner) findViewById(R.id.spinner);
        final Spinner timeChoice=(Spinner) findViewById(R.id.spinner2);
        idd=(EditText)findViewById(R.id.idd);
        Button scale=(Button) findViewById(R.id.scale);
        Button diary=(Button) findViewById(R.id.diary);
        Button send=(Button) findViewById(R.id.send);
        scale.setOnClickListener(new go_to_scale());
        diary.setOnClickListener(new go_to_diary());
        send.setOnClickListener(new go_to_send());

        mTimeTextView = new TimerFrame(this);



        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
        @Override
           public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });
        AssetFileDescriptor file = this.getResources().openRawResourceFd(
                R.raw.heal8);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(),
                    file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(1,1);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            mediaPlayer = null;
        }

        final MediaPlayer finalMediaPlayer1 = mediaPlayer;
        instruction1.setText("请填写被试编号");
        instruction2.setText("请选择测试时间");

        newStream.setFullsize(300);//set size for storage
        newStream.setmList(1000);


        timeChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String[] languages = getResources().getStringArray(R.array.time);
               //Toast.makeText(MainActivity.this, "你选择的是:"+languages[pos], Toast.LENGTH_SHORT).show();
                time=languages[pos];//选择的时间
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        paceChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String[] languages = getResources().getStringArray(R.array.pace);
                //Toast.makeText(MainActivity.this, "你选择的是:"+languages[pos], Toast.LENGTH_SHORT).show();
                pace=languages[pos];//选择的时间
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });




        myContext1 = String.valueOf(Environment.getExternalStorageDirectory());


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AccelerometerService.class.getName());
        wakeLock.acquire();//屏幕

        doBindService();

        boolean isAllGranted = checkPermissionAllGranted(//check
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );

        if (!isAllGranted) {
            ActivityCompat.requestPermissions(//request
                    this,
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    MY_PERMISSION_REQUEST_CODE
            );
        }



        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //弹出填写速度的窗口
                //检查是否填写完整，检查该时间段是否有数据，检查是否做过了scale
                try {
                    //可能是time的问题
                    File f = new File(Environment.getExternalStorageDirectory() + "/ActivityRecorder/" + time);//检查的是dir
                    if (!f.exists()) {//要检查的是有没有这个东西
                        //如果没有这个dir，说明没有进行问卷测试
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("提醒");

                        builder.setMessage("您还未填写问卷，请先填写问卷再进行步态测试！");

                        builder.setPositiveButton("填写问卷", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //go to 问卷
                                Intent intent = new Intent(MainActivity.this
                                        , survey_activity.class);
                                //bundle
                                Bundle bundle = new Bundle();
                                //传递name参数为tinyphp
                                bundle.putString("participant_id", idd.getText().toString());
                                bundle.putString("time of test", time);
                                intent.putExtras(bundle);//根据时间选择出现哪一个问卷
                                startActivity(intent);
                            }

                        });
                        builder.show();
                    }else {
                        if(s!=null) {
                            accData.setText("准备就绪，可以开始测试");
                            doBindService();
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                startService(new Intent(MainActivity.this,
                        AccelerometerService.class));
                //notificationMain.createInfoNotification("Start service");

            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    s.unregistered();
                stopService(new Intent(MainActivity.this,
                        AccelerometerService.class));
                doUnbindService();
                mTimeTextView.stopService();
                //notificationMain.createInfoNotification("Stop service");
                accData.setText("实验停止");
                time_tag=0;

            }
        });

        if(threadFlag==false&&mThread ==null)
        {
            threadFlag = true;
            mThread = new Thread() {
                public void run() {
                    while (threadFlag) {
                        if (newStream.isFullFlag()) {
                            //Collections.copy(copyMList,mList);
                            threadFlag=false;//end thread
                            newStream.setFullFlag();//设置成没有
                            ArrayList l1= new ArrayList(newStream.getmList());//l1 becomes the cache
                            newStream.getmList().clear();
                            //writeToNewFile(newStream.getCopyMList());
                            writeToNewFile(l1);
                            threadFlag=true;
                        }

                    }
                }
            };
            mThread.start();
        }



        btnShow.setOnClickListener(new View.OnClickListener() {//开始实验

            @Override
            public void onClick(View v) {

                if (s!= null && s.getSensorStatus()) {
                        filename = idd.getText().toString() + "+" + time + "+" + pace + ".txt";
                        accData.setText("正在测试中...");
                        record = true;
                        mTimeTextView.setTime("0", "00", "05", "00");//开始五分钟
                        // write to DataCollect.txt every second
                        textFileLogger = new Runnable() {
                            @Override
                            public synchronized void run() {
                                if (record) {
                                    try {
                                        JSONArray jsonArray = new JSONArray(s.getDataFromAccelerometr());
                                        newStream.updataStream(jsonArray);//update
                                        index++;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    //判断
                                    if (mTimeTextView.stopService()) {
                                        //if (index == limit) {
                                        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        long[] pattern = {100, 400, 100, 400};   // 停止 开启 停止 开启
                                        vibrator.vibrate(pattern, -1);
                                        record = false;
                                        index = 0;
                                        s.unregistered();//取消注册
                                        if( s!=null) {//s still alive,not mIsbound，其实不需要判定
                                            //提示，call stop service,弹出对话框
                                            stopService(new Intent(MainActivity.this,
                                                    AccelerometerService.class));
                                            doUnbindService();
                                            if (finalMediaPlayer1 != null) {
                                                finalMediaPlayer1.start();//play ring
                                            }
                                            accData.setText("实验结束");//更改
                                        }
                                    }
                                    //Repeats the logging every 0.05 second
                                    mHandler.postDelayed(this, 50);//why repeats?
                                }
                            }
                        };
                        //Starts the logging after 10 second
                        mHandler.postDelayed(textFileLogger, 50);
                    } else {
                        //enter again still service, check sensor manager
                        accData.setText("遇到问题，无法测试，请联系主试。");//success!!!
                        record = false;
                    }

            }



        });




    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        super.onDestroy();
        doUnbindService();//conn表示ServiceConnection 对象
        if (mThread!=null){
            mThread.interrupt();
            mThread=null;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    void doBindService() {
        bindService(new Intent(this, AccelerometerService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        if(mThread!=null){
            mThread.interrupt();
            mThread=null;
        }
    }

    //页面切换
    class go_to_diary implements View.OnClickListener{//填写日志

        @Override
        public void onClick(View arg0){
            Intent intent2 = new Intent(MainActivity.this,diary_activity.class);
            startActivity(intent2);
        }
    }

    class go_to_scale implements View.OnClickListener{

        @Override
        public void onClick(View arg0){
            if(TextUtils.isEmpty(idd.getText())){//有没有写
                //弹出提醒框
                AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);//进入步态测试
                builder.setTitle("填写实验序号");
                builder.setMessage("请填写实验序号，才能填写问卷");
                builder.setPositiveButton("确定", null);
                builder.show();

            }else {
                AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);//进入步态测试
                builder.setTitle("注意测试时间");
                builder.setMessage("您选择的是"+time+"，确认填写该时间问卷或更改时间。");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //check file 是否存在
                        Intent intent1 = new Intent(MainActivity.this, survey_activity.class);
                        Bundle bundle = new Bundle();
                        //传递name参数为tinyphp
                        bundle.putString("participant_id", idd.getText().toString());
                        bundle.putString("time of test", time);
                        intent1.putExtras(bundle);//根据时间选择出现哪一个问卷
                        startActivity(intent1);
                    }
                });

                builder.setNegativeButton("重新更改时间", null);
                builder.show();
            }
        }
    }

    class go_to_send implements View.OnClickListener{

        @Override
        public void onClick(View arg0){
            //check是否有participant ID,应该为数字
            if(TextUtils.isEmpty(idd.getText())){//有没有写
                //Toast.makeText(MainActivity.this,String.format("please enter participant ID"),Toast.LENGTH_SHORT).show();
                //弹出提醒
                AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);//进入步态测试
                builder.setTitle("填写实验序号");
                builder.setMessage("请填写实验序号，才能提交数据");
                builder.setPositiveButton("确定", null);
                builder.show();
            }else {
                Intent intent4 = new Intent(MainActivity.this, send_activity.class);
                //用Bundle携带数据
                Bundle bundle = new Bundle();
                //传递name参数为tinyphp
                bundle.putString("participant_id", idd.getText().toString());
                bundle.putString("time of test",time);
                intent4.putExtras(bundle);
                startActivity(intent4);
            }
        }
    }

    private void writeToNewFile(ArrayList f) {
            try {
                FileOutputStream fos = openFileOutput(filename,
                        Context.MODE_APPEND);
                String storageState = Environment.getExternalStorageState();
                if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                    String filePath = myContext1 + "/ActivityRecorder/" + time +"/"+ filename;//new path
                    File file = new File(filePath);
                    FileWriter fw = new FileWriter(file,true);//追加
                    BufferedWriter bufw = new BufferedWriter(fw);
                    //bufw.write(s.substring(1,(s.length()-2)));
                    for (int i = 0; i < f.size(); i++) {
                        //write to file
                        bufw.write(f.get(i).toString());
                        //bufw.write(String.valueOf(f));
                    }
                    bufw.flush();
                    bufw.close();//只有最后的数据了
                    fw.close();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

        }
    }




}

