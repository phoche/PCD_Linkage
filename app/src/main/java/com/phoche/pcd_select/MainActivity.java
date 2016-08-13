package com.phoche.pcd_select;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PCDPopUpWindow.OnCheckChangeListener {
    private Button      bt_select;
    private PopupWindow popupWindow;
    private File        file;
    private TextView    tv_province;
    private TextView    tv_city;
    private TextView    tv_district;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initData();

        initEvent();
    }

    private void initEvent() {
        bt_select.setOnClickListener(this);
    }

    private void initData() {

        initDB();

    }

    /**
     * 初始化数据库
     * 第一次运行时把数据库文件copy到file目录
     */
    private void initDB() {
        file = new File(getFilesDir(), "city.db");
        if (!file.exists()) {
            new Thread() {

                public void run() {
                    InputStream is = null;
                    FileOutputStream fos = null;
                    try {
                        is = getAssets().open("city.db");
                        fos = new FileOutputStream(file);

                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        try {
                            if (is != null) {

                                is.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                        try {
                            if (fos != null) {
                                fos.close();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }.start();
        }

    }

    private void initView() {
        bt_select = (Button) findViewById(R.id.bt_select);
        tv_province = (TextView) findViewById(R.id.tv_province);
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_district = (TextView) findViewById(R.id.tv_district);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_select:

                showPopUpWindow();
                break;
        }

    }

    /**
     * 显示三级联动的PopUpWindow
     */
    private void showPopUpWindow() {
        popupWindow = new PCDPopUpWindow(this, file, this);

        // 窗口显示的位置
        popupWindow.showAsDropDown(bt_select);
    }


    /**
     * 以下是选择item的回调
     * @param text
     */
    @Override
    public void setCityText(String text) {
        tv_city.setText(text);
    }

    @Override
    public void setProvinceText(String text) {
        tv_province.setText(text);
    }

    @Override
    public void setDistrictText(String text) {
        tv_district.setText(text);
    }
}
