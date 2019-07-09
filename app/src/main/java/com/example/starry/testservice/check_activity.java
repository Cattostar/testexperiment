package com.example.starry.testservice;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.starry.testservice.R;
import com.example.starry.testservice.utils.TableAdapter;
import com.example.starry.testservice.utils.TableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;




public class check_activity extends AppCompatActivity {

    private List<Content> contentList;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        tableLayout = (TableLayout) findViewById(R.id.main_table);
        initContent();//read in data
//        firstRowAsTitle();
        firstColumnAsTitle();
    }

    private void initContent() {
        contentList = new ArrayList<>();
        //read in,column
        File mFile= new File(Environment.getExternalStorageDirectory() + "/ActivityRecorder/ActivityTime.txt");//文件读取
      /*  BufferedReader br;
        try {
            br= new BufferedReader(new FileReader(mFile));
            //check how many arrays,and add arrays

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        contentList.add(new Content("事件","开始时间", "结束时间","疲劳程度"));
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(mFile));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                JSONObject object = new JSONObject(line);//all string builder
                //stringBuilder.append(line);
                contentList.add(new Content(object.getString("事件"),object.getString("开始时间"),object.getString("结束时间"),object.getString("疲劳程度")));
            }
            bufferedReader.close();
            //JSONObject jsonObject = new JSONObject(stringBuilder.toString());//all string builder
            //JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            //JSONArray jsonArray = new JSONArray(stringBuilder.toString());//all string builder
            //Log.i("TESTJSON", "cat=" + jsonObject.getString("cat"));
            //JSONArray jsonArray = jsonObject.getJSONArray("activity");//no name
            /*for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);//the number ith to json object
                contentList.add(new Content(object.getString("事件"),object.getString("开始时间"),object.getString("结束时间"),object.getString("疲劳程度")));//display
            }*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //将第一行作为标题
    private void firstRowAsTitle() {
        //fields是表格中要显示的数据对应到Content类中的成员变量名，其定义顺序要与表格中显示的相同
        final String[] fields = {"事件","开始时间", "结束时间","疲劳程度"};
        tableLayout.setAdapter(new TableAdapter() {
            @Override
            public int getColumnCount() {
                return fields.length;
            }

            @Override
            public String[] getColumnContent(int position) {
                int rowCount = contentList.size();
                String contents[] = new String[rowCount];
                try {
                    Field field = Content.class.getDeclaredField(fields[position]);
                    field.setAccessible(true);
                    for (int i = 0; i < rowCount; i++) {
                        contents[i] = (String) field.get(contentList.get(i));
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return contents;
            }
        });
    }

    //将第一列作为标题
    private void firstColumnAsTitle() {
        tableLayout.setAdapter(new TableAdapter() {
            @Override
            public int getColumnCount() {
                return contentList.size();
            }
            @Override
            public String[] getColumnContent(int position) {
                return contentList.get(position).toArray();
            }
        });
    }


    public static class Content {

        private String 事件;
        private String 开始时间;
        private String 结束时间;
        private String 疲劳程度;


        public Content(String 事件, String 开始时间, String 结束时间, String 疲劳程度) {
            this.事件 = 事件;
            this.开始时间 = 开始时间;
            this.结束时间 = 结束时间;
            this.疲劳程度 = 疲劳程度;

        }

        public String[] toArray() {
            return new String[]{事件, 开始时间, 结束时间, 疲劳程度};
        }

    }

}
