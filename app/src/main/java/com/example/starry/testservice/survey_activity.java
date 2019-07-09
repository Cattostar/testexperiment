package com.example.starry.testservice;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.example.starry.testservice.models.QuestionModel;
import com.example.starry.testservice.utils.ToastUtils;
import com.example.starry.testservice.utils.UiUtils;
import com.example.starry.testservice.weight.FlowRadioGroup;


import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.starry.testservice.utils.JsonHelper.loadJSONFromAsset;


public class survey_activity extends Activity implements OnClickListener {
    private ImageButton btnImgBack;
    private ArrayList<QuestionModel> questionList;
    @SuppressLint("UseSparseArrays")
    private Map<String, String> result = new HashMap<String, String>();
    private TextView tvSubmit, tvBack;
    private JSONObject jsonObject;
    private String time_period;
    private String numbervalue;

    private LinearLayout llSurveyMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_survey);
        Bundle bundle = this.getIntent().getExtras();
        //接收name值
        numbervalue = bundle.getString("participant_id");//id number
        time_period= bundle.getString("time of test");//before or after
        initLayout();
        //创建路径
        makeFilePath(Environment.getExternalStorageDirectory() + "/ActivityRecorder/", time_period);//认为有斜杠
        String json= loadJSONFromAsset(this, time_period.toString());//获取早上或者晚上的问卷,加一个tostring
        analysisJson(json);
        createLayout(questionList);
        submitResultBind();
        }

    /**
     *
     * <p>
     * Title: initLayout
     * </p>
     * <p>
     * Description:资源初始化
     * </p>
     */
    @SuppressLint("ResourceType")
    private void initLayout() {
        llSurveyMain = (LinearLayout) findViewById(R.id.ll_survey_main);
        btnImgBack = (ImageButton) findViewById(R.id.back_survey);
        tvBack = (TextView) findViewById(R.id.tv_back_survey);
        btnImgBack.setOnClickListener(this);
        tvBack.setOnClickListener(this);
    }

    /**
     *
     * <p>
     * Title: analysisJson
     * </p>
     * <p>
     * Description: 网络返回问题JSON解析
     * </p>
     *
     * @param result
     */
    private void analysisJson(String result) {
        questionList = (ArrayList<QuestionModel>) JSONArray.parseArray(result, QuestionModel.class);
    }

    /**
     * <p>
     * Title: submitResult
     * </p>
     * <p>
     * Description: 数据提交
     * </p>
     */
    private void submitResultBind() {

        // 提交按钮点击事件绑定
        tvSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                int checkResult = checkSelectedAll(questionList.size(), result);
                // 问题是否回答完整检查
                if (checkResult < 0) {
                    //ToastUtils.show(survey_activity.this, "正在提交");
                    jsonObject= new JSONObject(result);
                    //把result存入文件里
                    boolean a=saveDataToSDCard("surveyResult+"+time_period+".json", jsonObject);//存到那个文件当中去！！！
                    //System.out.println(jsonObject.toString());
                    if (a== Boolean.TRUE){
                        AlertDialog.Builder builder= new AlertDialog.Builder(survey_activity.this);//进入步态测试

                        builder.setTitle("提交状态");

                        builder.setMessage("您已经成功提交问卷，可以进行步态测试");

                        builder.setPositiveButton("进入步态测试", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //go to 问卷
                                Intent intent = new Intent(survey_activity.this
                                        ,MainActivity.class);
                                startActivity(intent);
                            }
                        });

                        builder.show();
                    }
                } else {
                    // 提示未选择的问题,toast都改成提示框
                    ToastUtils.show(survey_activity.this, "请填写问题" + (checkResult + 1));
                }
            }
        });

    }

    /**
     * <p>
     * Title: createLayout
     * </p>
     * <p>
     * Description: 样式生成
     * </p>
     */
    /**
     * <p>
     * Title: createLayout
     * </p>
     * <p>
     * Description:
     * </p>
     *
     * @param questionList
     */
    private void createLayout(ArrayList<QuestionModel> questionList) {
        for (int i = 0; i < questionList.size(); i++) {

            // ####生成item框架####
            LinearLayout itemSurveyLayout = new LinearLayout(this);
            itemSurveyLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            // 设置item宽度、高度
            // LinearLayout.LayoutParams paramsItem = new
            // LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            // UiUtils.dip2px(this, 100));
            LinearLayout.LayoutParams paramsItem = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            // 设置item margin
            paramsItem.setMargins(0, UiUtils.dip2px(this, 10), 0, 0);
            itemSurveyLayout.setPadding(UiUtils.dip2px(this, 5), 0, UiUtils.dip2px(this, 5), 0);
            itemSurveyLayout.setLayoutParams(paramsItem);
            itemSurveyLayout.setBackgroundColor(Color.parseColor("#00000000"));
            itemSurveyLayout.setOrientation(LinearLayout.VERTICAL);

            // ####生成问题TextView####
            TextView question = new TextView(this);
            LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsTextView.setMargins(0, UiUtils.dip2px(this, 10), 0, UiUtils.dip2px(this, 10));
            question.setLayoutParams(paramsTextView);
            question.setTextSize(UiUtils.sp2px(this, 5.2f));
            question.setText((questionList.get(i).getQuestionNo()) + "." + questionList.get(i).getQuestionName());
            // 渲染问题到item框架
            itemSurveyLayout.addView(question);

            // 问题类型0，没有具体选择答案，星级评价
            if (questionList.get(i).getQuestionType() == 0) {
                // *********************************************************************
                RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyle);
                LinearLayout.LayoutParams paramsRastingBar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                paramsRastingBar.setMargins(UiUtils.dip2px(this, 10), 0, 0, 0);
                ratingBar.setId(i);// 设置ID
                ratingBar.setLayoutParams(paramsRastingBar);
                ratingBar.setNumStars(5);// 设置最大星星数量
                ratingBar.setStepSize(1.0f);// 设置星星步长
                ratingBar.setRating(0);// 设置默认星星得分
                // ratingBar.setProgress(progress);
                // 设置监听事件
                ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        //ToastUtils.show(survey_activity.this, "星星：" + ratingBar.getId() + "分数" + rating);
                        result.put(String.valueOf(ratingBar.getId()), String.valueOf(rating));
                    }
                });

                itemSurveyLayout.addView(ratingBar);
                // 问题类型1，有具体选择项，使用Radio处理
            } else if (questionList.get(i).getQuestionType() == 1) {
                // ###################################################################
                // ####生成答案选项RadioGroup###
                final FlowRadioGroup group = new FlowRadioGroup(this); // 实例化单选按钮组
                LinearLayout.LayoutParams paramsGroup = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                paramsGroup.setMargins(UiUtils.dip2px(this, 7), 0, 0, 0);
                group.setLayoutParams(paramsGroup);
                group.setId(i);
                group.setOrientation(LinearLayout.HORIZONTAL);
                // 添加单选按钮
                for (int j = 0; j < questionList.get(i).getQuestionOption().size(); j++) {
                    final RadioButton radio = new RadioButton(this);
                    // 设置字体大小
                    radio.setTextSize(UiUtils.dip2px(survey_activity.this, 5));
                    // 设置radio文字
                    radio.setText(questionList.get(i).getQuestionOption().get(j));
                    // 设置radio监听事件
                    radio.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //ToastUtils.show(survey_activity.this, "Group" + String.valueOf(group.getId()) + "&Radio：" + radio.getText().toString());
                            result.put(String.valueOf(group.getId()), radio.getText().toString());
                        }
                    });
                    group.addView(radio);
                }
                // 渲染RadioGroup到item框架
                itemSurveyLayout.addView(group);
            }

            // 附加到item
            llSurveyMain.addView(itemSurveyLayout);
        }

        // ####添加提交按钮
        tvSubmit = new TextView(this);
        LinearLayout.LayoutParams paramsTvSubmit = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UiUtils.dip2px(this, 50));
        paramsTvSubmit.setMargins(0, UiUtils.dip2px(this, 10), 0, 0);
        tvSubmit.setText("提交");
        tvSubmit.setTextColor(Color.parseColor("#02A0F2"));
        tvSubmit.setTextSize(UiUtils.sp2px(this, 6.2f));
        tvSubmit.setGravity(Gravity.CENTER);
        tvSubmit.setBackgroundColor(Color.parseColor("#AAFFFFFF"));
        tvSubmit.setLayoutParams(paramsTvSubmit);
        llSurveyMain.addView(tvSubmit);

    }

    /**
     *
     *
     *
     * <p>
     * Title: checkSelectAll
     * </p>
     * <p>
     * Description: 检查是否全部选择了
     * </p>
     *
     * @param questionNum
     *            调查问卷问题数量,从0开始
     * @param result
     *            选择结果的map
     * @return 如果全部填写完成返回-1 ，否则返回问题所在序号
     */
    protected int checkSelectedAll(int questionNum, Map<String, String> resultMap) {
        for (int i = 0; i < questionNum; i++) {
            if (!resultMap.containsKey(String.valueOf(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back_survey:
               survey_activity.this.finish();
                break;
            case R.id.back_survey:
                survey_activity.this.finish();//survey的activity有finish
                break;

            default:
                break;
        }

    }


    /*
     *
     *
     * @save map to sdcard
     */
    public boolean saveDataToSDCard(String fileName, JSONObject jsonObject2){
        boolean isAvailable = false;    //SD是否可读
        FileOutputStream fileOutputStream = null;
        File filedir= new File(Environment.getExternalStorageDirectory()+"/ActivityRecorder/"+time_period);
        if( !filedir.exists() )
            filedir.mkdirs();
        else if( !filedir.isDirectory() && filedir.canWrite() ){
            filedir.delete();
            filedir.mkdirs();
        }
        //创建File对象
        File file = new File(Environment.getExternalStorageDirectory()+"/ActivityRecorder/"+time_period,fileName);//放到对应的文件夹下
        //File listnames = new File(this.getFilesDir() + File.separator + "Lists");

        //判断SD卡是否可读写
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            isAvailable = true;
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(jsonObject2.toString().getBytes());
                if(fileOutputStream != null){
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return isAvailable;
    }

    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

}
