package com.phoche.pcd_select;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.phoche.pcd_select.adapter.PcdAdapter;
import com.phoche.pcd_select.bean.AreaBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by phoche on 16-8-12
 */
public class PCDPopUpWindow extends PopupWindow implements AdapterView.OnItemClickListener {

    private final String mTable_province = "province";
    private final String mTable_city     = "city";
    private final String mTable_district = "district";
    private final int    mLoad_province  = 100;
    private final int    mLoad_city      = 101;
    private final int    mLoad_district  = 102;
    private File     mDbFile;     //城市数据库的目录
    private Context  mContext;
    private View     mView;
    private ListView mLv_province; // 省列表
    private ListView mLv_city;      // 城市列表
    private ListView mLv_district;  // 地区列表
    private int            proCityPosition     = -1;   // 上一个城市的索引
    private int            proProvincePosition = -1;   // 上一个省的索引
    private int            proDistrictPosition = -1;   // 上一个地区的索引
    private List<AreaBean> mProvinceDatas      = new ArrayList<>(); // 省份集合
    private List<AreaBean> mCityDatas          = new ArrayList<>(); // 城市集合
    private List<AreaBean> mDistrictDatas      = new ArrayList<>(); // 地区集合
    private       PcdAdapter            mCityAdapter;
    private       PcdAdapter            mProvinceAdapter;
    private       PcdAdapter            mDistrictAdapter;
    private final OnCheckChangeListener mOnCheckChangeListener;

    /**
     * 用来对listview设置adapter的handler
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            List<AreaBean> list = (List<AreaBean>) msg.obj;
            switch (msg.what) {
                case mLoad_province:
                    // 加载省列表
                    mProvinceDatas.addAll(list);
                    mProvinceAdapter = new PcdAdapter(mContext, mProvinceDatas);
                    mLv_province.setAdapter(mProvinceAdapter);
                    break;
                case mLoad_city:
                    // 加载城市列表
                    mCityDatas.addAll(list);
                    mCityAdapter = new PcdAdapter(mContext, mCityDatas);
                    mLv_city.setAdapter(mCityAdapter);
                    break;
                case mLoad_district:
                    // 加载地区列表
                    mDistrictDatas.addAll(list);
                    mDistrictAdapter = new PcdAdapter(mContext, mDistrictDatas);
                    mLv_district.setAdapter(mDistrictAdapter);
                    break;
            }
        }
    };
    private SQLiteDatabase mDb;

    // 因为要先初始化数据库
    // 所以数据库文件在创建PopUpWindow时传入
    public PCDPopUpWindow(Context context, File file, OnCheckChangeListener listener) {
        this.mDbFile = file;
        this.mContext = context;
        this.mOnCheckChangeListener = listener;
        // PopUpWindow的布局由三个ListView组成
        mView = View.inflate(mContext, R.layout.pop_pcd_listview, null);
        setContentView(mView);
        // 设置宽高
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

        // 使其可点击
        // 点击外部可以消失
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable());

        initView();

        initEvent();

        // 先初始化省的列表
        loadCityData(mTable_province, null);
    }


    public void initEvent() {
        mLv_province.setOnItemClickListener(this);
        mLv_city.setOnItemClickListener(this);
        mLv_district.setOnItemClickListener(this);

    }


    private void initView() {
        mLv_province = (ListView) mView.findViewById(R.id.lv_province);
        mLv_city = (ListView) mView.findViewById(R.id.lv_city);
        mLv_district = (ListView) mView.findViewById(R.id.lv_district);

    }

    /**
     * 选择地区的事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv_province:
                // 点击省份列表时要将城市和地区的数据清空
                // 并将城市的前一个索引设为-1,防止选择城市只有一个的省时发生越界
                if (mOnCheckChangeListener != null) {
                    mOnCheckChangeListener.setCityText("");
                    mOnCheckChangeListener.setDistrictText("");
                }
                mDistrictDatas.clear();
                mCityDatas.clear();
                proCityPosition = -1;

                // 根据是否有上一个选择的省份
                // 有的话就将上的状态置为false,以改变背景
                if (proProvincePosition != -1) {
                    AreaBean proAreaBean = (AreaBean) parent.getItemAtPosition(proProvincePosition);
                    proAreaBean.isChoose = false;

                }
                // 将当前的省份赋给上一个省份
                // 并把当前的状态设为已选中
                proProvincePosition = position;
                AreaBean areaBean = (AreaBean) parent.getItemAtPosition(position);
                areaBean.isChoose = true;

                // 监听选择的省份,把数据用接口传给MainActivity
                if (mOnCheckChangeListener != null) {
                    mOnCheckChangeListener.setProvinceText("省 : " + areaBean.name);
                }
                // 更新省份的ListView
                mProvinceAdapter.notifyDataSetChanged();
                // 根据当前省数据的code加载城市列表
                // 从数据库中查询城市数据
                loadCityData(mTable_city, areaBean.code);
                break;

            case R.id.lv_city:
                // 选择城市列表需要将之前选择的地区清除
                if (mOnCheckChangeListener != null) {
                    mOnCheckChangeListener.setDistrictText("");
                }
                // 更新城市item的背景
                // 同省份
                if (proCityPosition != -1) {
                    AreaBean cityAreaBean = (AreaBean) parent.getItemAtPosition(proCityPosition);
                    cityAreaBean.isChoose = false;
                }

                proCityPosition = position;
                AreaBean cityAreaBean = (AreaBean) parent.getItemAtPosition(position);
                cityAreaBean.isChoose = true;


                if (mOnCheckChangeListener != null) {
                    mOnCheckChangeListener.setCityText("市 : " + cityAreaBean.name);
                }
                mCityAdapter.notifyDataSetChanged();

                // 根据市级数据的code加载区域列表

                loadCityData(mTable_district, cityAreaBean.code);

                break;
            // 选择地区的item,同省份
            case R.id.lv_district:
                if (proDistrictPosition != -1) {
                    AreaBean districtAreaBean = (AreaBean) parent.getItemAtPosition(proDistrictPosition);
                    districtAreaBean.isChoose = false;
                }
                proDistrictPosition = position;

                AreaBean districtAreaBean = (AreaBean) parent.getItemAtPosition(position);
                districtAreaBean.isChoose = true;

                if (mOnCheckChangeListener != null) {
                    mOnCheckChangeListener.setDistrictText("地区 : " + districtAreaBean.name);
                }
                mDistrictAdapter.notifyDataSetChanged();
                break;
        }
    }

    // 开启线程来加载数据
    public void loadCityData(final String table_name, final String pcode) {
        new Thread() {
            @Override
            public void run() {

                queryPcdDB(table_name, pcode);

            }
        }.start();
    }


    /**
     * 用来点击选择显示文本的回调
     */
    public interface OnCheckChangeListener {

        void setCityText(String text);

        void setProvinceText(String text);

        void setDistrictText(String text);

    }


    /**
     * 查询数据库的操作
     *
     * @param table 表名
     * @param pcode 省市地区的code
     * @return 各省名称的数据
     */
    public void queryPcdDB(String table, String pcode) {
        // 打开已有的数据库文件
        if (mDb == null) {

            mDb = SQLiteDatabase.openDatabase(mDbFile.getAbsolutePath(),
                    null, SQLiteDatabase.OPEN_READWRITE);
        }

        List<AreaBean> datas = new ArrayList<>();

        String sql = " select  name,code from " + table;
        String[] selectionArgs = null;
        // 查询省份数据时不需要pcode
        if (pcode != null) {
            sql = " select  name,code from " + table + " where pcode=" + pcode;
        }
        Cursor cursor = mDb.rawQuery(sql, selectionArgs);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                byte[] blob = cursor.getBlob(cursor.getColumnIndex("name"));
                String name = "";
                try {
                    // 由于是在网络上下载的数据库文件,里面的编码不是UTF8的,这里进行的转码
                    name = new String(blob, "gbk");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String code = cursor.getString(cursor.getColumnIndex("code"));

                AreaBean areaBean = new AreaBean(name, code);
                datas.add(areaBean);
            }
        }
        cursor.close();

        // 查询在做是在子线程中的,查询结束后发送message设置adapter
        Message msg = Message.obtain();
        msg.obj = datas;

        switch (table) {
            case mTable_province:
                msg.what = mLoad_province;
                break;

            case mTable_city:
                msg.what = mLoad_city;
                break;

            case mTable_district:
                msg.what = mLoad_district;
                break;
        }

        mHandler.sendMessage(msg);

    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        super.setOnDismissListener(onDismissListener);
        mDb.close();
    }
}
