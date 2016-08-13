省市区三级联动
============

使用PopUpWindow的方式弹出选择框
采用垂直的三个listview分别显示省,市,区<br>
数据使用的是数据库,放在了assets里,第一次运行是会copy到当前的file目录<br>
用PopUpWindow和activity分离只需要创建自定义的PopUP就可以显示
在item中点击的数据通过监听传过来

![gif](https://github.com/phoche/PCD_Linkage/blob/master/art/pcd.gif)

```
	// 创建popupwindow是传入数据库的文件对象
	popupWindow = new PCDPopUpWindow(this, file, this);
    // 窗口显示的位置
    popupWindow.showAsDropDown(bt_select);

```

```
	// 在自定义的popupwindow里的监听回调,直接显示在activity的textview中
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
```
