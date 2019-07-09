package com.example.starry.testservice;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class send_activity extends AppCompatActivity {

    private Button Connect_Server, Send_server, Send_email;
    private String RootPath="/";
    private List<String> items = null;//存放名称
    private List<String> paths = null;//存放路径
    private String dirpath = Environment.getExternalStorageDirectory() + "/";
    private String name = null;
    private String time_period = null;
    private String email = "18643196298@163.com";
    private FTPUtils ftpUtils = null;
    private TextView walkRow, diaryRow, scaleRow;
    private TextView walkStatus, diaryStatus, scaleStatus;
    private TextView walkCheck, diaryCheck, scaleCheck;
    private TextView wifi;
    private AlertDialog.Builder mDialog;
    private Thread mThread;//only thread instance
    private boolean flag = false;
    private File zipfile;
    private boolean flag2 =false;
    private Boolean w1=false,s1=false,d1=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        walkRow = (TextView) findViewById(R.id.WalkRow);
        scaleRow = (TextView) findViewById(R.id.ScaleRow);
        diaryRow = (TextView) findViewById(R.id.DiaryRow);
        diaryStatus = (TextView) findViewById(R.id.DiaryStatus);
        scaleStatus = (TextView) findViewById(R.id.ScaleStatus);
        walkStatus = (TextView) findViewById(R.id.WalkStatus);
        diaryCheck = (TextView) findViewById(R.id.DiaryCheck);
        scaleCheck = (TextView) findViewById(R.id.ScaleCheck);
        walkCheck = (TextView) findViewById(R.id.WalkCheck);
        wifi = (TextView) findViewById(R.id.textView2);

        //check if killed


        //check wifi and make warning
        Boolean ssr1 = WifiAccess.isNetworkAvailable(this);
        Boolean ssr2 = WifiAccess.isWiFi(this);
        if (ssr1 && ssr2) {
            wifi.setText("检查有网络可以提交。");

        } else {
            wifi.setText("请先连接网络！！！");
        }

        if (mThread == null && flag == false) {
            flag = true;
            mThread = new Thread() {
                public void run() {
                    while (flag) {
                        try {
                            if (Environment.getExternalStorageState().
                                    equals(Environment.MEDIA_MOUNTED)) {
                                File path = new File(dirpath + "ActivityRecorder");
                                File[] files = path.listFiles();// 读取文件夹下文件
                                File path1 = new File (dirpath + "ActivityRecorder/工作前");
                                File path2 = new File (dirpath + "ActivityRecorder/工作后");
                                File[] files1 = path1.listFiles();// 读取文件夹下文件
                                File[] files2 = path2.listFiles();// 读取文件夹下文件
                                String files1list1 = getFileName(files1,".json");
                                String files1list2 = getFileName(files1,".txt");
                                String files1list3 = getFileName(files2,".json");
                                String files1list4 = getFileName(files2,".txt");
                                if (files.length==4) {//一定填写了
                                    d1=true;
                                    //check each file
                                } else{
                                    d1=false;
                                }

                                if(files1list1!="" && files1list3!=""){
                                    s1=true;
                                }else{
                                    s1=false;
                                }

                                if(files1.length>2 && files2.length>2)
                                {
                                    w1=true;

                                }else{
                                    w1=false;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("value", "成功了！");
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }
            };
            mThread.start();
        }


        Connect_Server = (Button) findViewById(R.id.button3);
        Connect_Server.setOnClickListener(new connect_server());
        Send_server = (Button) findViewById(R.id.button4);
        Send_server.setOnClickListener(new send_result_server());
        Send_email = (Button) findViewById(R.id.button);
        Send_email.setOnClickListener(new send_result_email());
        //新页面接收数据
        Bundle bundle = this.getIntent().getExtras();
        //接收name值
        name = bundle.getString("participant_id");
        time_period = bundle.getString("time of test");
        RootPath = dirpath + "ActivityRecorder";



    }


    class send_result_email implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            String time = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());//在上传的时候生成今天的日期
            zipfile = new File(dirpath + name + "," + time + ".zip");//拿到ID
            try {
                //testDir(RootPath, zipfile.getPath());
                ziputil.zip(Environment.getExternalStorageDirectory().getPath() + "/ActivityRecorder", dirpath + name + "," + time + ".zip");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            String[] recipients = new String[]{email, "",};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "This is my data!");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zipfile));
            emailIntent.setType("text/plain");//zip??
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }

    }


    // /get files from SD card, maybe in the same directory;
    class connect_server implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {

            String time = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());//在上传的时候生成今天的日期
            zipfile = new File(dirpath + name + "," + time + ".zip");//拿到ID
            try {
                //testDir(RootPath, zipfile.getPath());
                ziputil.zip(Environment.getExternalStorageDirectory().getPath() + "/ActivityRecorder", dirpath + name + "," + time + ".zip");//zip ok!
            } catch (IOException e) {
                e.printStackTrace();
            }
            //上传文件到服务器，wifi在的情况下
            //new thread
            if (mThread == null && flag == false) {
                flag = true;//get on
                mThread = new Thread(networkTask);
                mThread.start();
            }



        }
    }



    class send_result_server implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {

        if (flag2 == true) {
            if (mThread == null && flag == false) {
                mThread = new Thread(uploadTask);
                mThread.start();
                flag = true;//get on
            }

        } else
        {
            mDialog = new AlertDialog.Builder(send_activity.this);
            mDialog.setTitle("提示");
            mDialog.setMessage("请先连接服务器!");
            mDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                    // TODO Auto-generated method stub
                }
            });
        }
        }
    }


    public void testDir(String args, String args1) throws IOException {
        // the file path need to compress
        File file = new File(args);
        ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(
                        args1));

        // judge the file is the directory
        if (file.isDirectory()) {
            // get the every file in the directory
            File[] files = file.listFiles();

            for (int i = 0; i < files.length; i++) {
                // new the BufferedInputStream
                BufferedInputStream bis = new BufferedInputStream(
                        new FileInputStream(
                                files[i]));
                // the file entry ,set the file name in the zip
                // file
                zos.putNextEntry(new ZipEntry(file
                        .getName()
                        + file.separator
                        + files[i].getName()));
                while (true) {
                    byte[] b = new byte[100];
                    int len = bis.read(b);
                    if (len == -1)
                        break;
                    zos.write(b, 0, len);
                }

                // close the input stream
                bis.close();
            }

        }
        // close the zip output stream
        zos.close();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mThread!=null || flag ==true || flag2 ==true){
            mThread.interrupt();
            mThread = null;
            flag = false;
            flag2 = false;
        }
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    if(files[i].exists()) {
                        try {
                            files[i].getCanonicalFile().delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (files[i].exists()){
                            this.deleteFile(files[i].getName());
                        }
                    }
                }
            }
        }
        return (path.delete());
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
            // TODO
            // UI界面的更新等相关操作

            if(mThread!= null) {
                flag = false;
                mThread.interrupt();
                mThread = null;
            }
            if ("传上了！".equals(val)) {
                flag2 = false;
                //check again
                mDialog = new AlertDialog.Builder(send_activity.this);
                mDialog.setTitle("提示");
                mDialog.setMessage("提交成功!");
                mDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                    }
                });
                mDialog.show();
                //delete folder
                File file1 = new File(Environment.getExternalStorageDirectory() + "/ActivityRecorder/工作前");//删掉工作前和工作后就可以
                File file2 = new File(Environment.getExternalStorageDirectory() + "/ActivityRecorder/工作后");
                File file3 = new File(Environment.getExternalStorageDirectory() + "/ActivityRecorder/ActivityTime.txt");
                //file3.delete();
                if (file1.exists()&& file2.exists() &&file3.exists()) {
                    boolean shanle1= deleteDirectory(file1);
                    boolean shanle2= deleteDirectory(file2);
                    boolean shanle3= file3.delete();
                    deleteDirectory(zipfile);//delete two files
                    if(shanle1&&shanle2 && shanle3) {
                        mDialog = new AlertDialog.Builder(send_activity.this);
                        mDialog.setTitle("提示");
                        mDialog.setMessage("已经清理内存!");
                        mDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                            }
                        });
                        mDialog.show();
                    } else{
                        mDialog = new AlertDialog.Builder(send_activity.this);
                        mDialog.setTitle("提示");
                        mDialog.setMessage("无法自动删除，请手动删除。");
                        mDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                            }
                        });
                        mDialog.show();
                    }
                }
                //finish();
            }
            /*else {
                //check again
                mDialog = new AlertDialog.Builder(send_activity.this);
                mDialog.setTitle("提示");
                mDialog.setMessage("提交失败!无法连接服务器");
                mDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                    }
                });
                mDialog.show();
            }*/
            if ("连上了！".equals(val)) {
                //check again
                mDialog = new AlertDialog.Builder(send_activity.this);
                mDialog.setTitle("提示");
                mDialog.setMessage("连接成功!");
                mDialog.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                    }
                });
                mDialog.show();
                //delete folder //finish();
                flag2 = true;
            }
            if("成功了！".equals(val)){
                if(d1) {
                    diaryCheck.setText("日志填写完成。");
                }else {
                    diaryCheck.setText("未填写日志！");
                }
                if(s1) {
                    scaleCheck.setText("问卷已填写全");
                }else {
                    scaleCheck.setText("问卷没有填写全");
                }
                if(w1) {
                    walkCheck.setText("步态测试已经完成");
                }else {
                    walkCheck.setText("步态测试未完成");
                }
            }

        }
    };

    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            // TODO
            // 在这里进行 http request.网络请求相关操作

            Boolean ftpStatus = this.InitFTPServerSetting();
            if (ftpStatus) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", "连上了！");
                msg.setData(data);
                handler.sendMessage(msg);
            } else {
                Toast.makeText(send_activity.this, "cannot connect to FTP service", Toast.LENGTH_SHORT).show();
            }
        }

        public boolean InitFTPServerSetting() {
            // TODO Auto-generated method stub
            ftpUtils = FTPUtils.getInstance();
            boolean flag = ftpUtils.initFTPSetting("211.159.170.137", 21, "thuhciuser", "1234567");
            return flag;
        }

    };


    Runnable uploadTask = new Runnable() {

        @Override
        public void run() {
            // TODO
            // 在这里进行 http request.网络请求相关操作
            String filePathString = zipfile.getPath().toString();
            String fileDes = zipfile.getName().toString();
            Boolean uploadStatus = ftpUtils.uploadFile(filePathString, fileDes);
            if (uploadStatus) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", "传上了！");
                msg.setData(data);
                handler.sendMessage(msg);
            } else {
                Toast.makeText(send_activity.this, "cannot upload file", Toast.LENGTH_SHORT).show();
            }
        }

    };

    private String getFileName(File[] files,String type) {
        String str = "";
        if (files != null) { // 先判断目录是否为空，否则会报空指针
            for (File file : files) {
                if (file.isDirectory()){//检查此路径名的文件是否是一个目录(文件夹)
                    Log.i("zeng", "若是文件目录。继续读1"
                            +file.getName().toString()+file.getPath().toString());
                    getFileName(file.listFiles(),type);
                    Log.i("zeng", "若是文件目录。继续读2"
                            +file.getName().toString()+ file.getPath().toString());
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(type)) {
                        String s=fileName.substring(0,fileName.lastIndexOf(".")).toString();
                        Log.i("zeng", "文件名txt：：   " + s);
                        str += fileName.substring(0,fileName.lastIndexOf("."))+"\n";
                    }
                }
            }

        }
        return str;
    }

}





