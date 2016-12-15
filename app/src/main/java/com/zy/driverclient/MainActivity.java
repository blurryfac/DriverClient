package com.zy.driverclient;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zy.driverclient.activity.AppointmentOrderListActivity;
import com.zy.driverclient.activity.DriverOrderActivity;
import com.zy.driverclient.activity.FatherActivity;
import com.zy.driverclient.activity.LoginActivity;
import com.zy.driverclient.activity.RealTimeListActivity;
import com.zy.driverclient.activity.RobbedAppointmentOrderActivity;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.QueryState;
import com.zy.driverclient.model.UpdateLocation;
import com.zy.driverclient.model.VersionMessage;
import com.zy.driverclient.service.UpdateService;
import com.zy.driverclient.utils.ExampleUtil;
import com.zy.driverclient.utils.SharedHelper;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class MainActivity extends FatherActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    public static boolean isForeground = false;
    private String passenger_phone;//乘客电话
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    LatLng ll;
    //地图相关
    MapView mMapView;
    BaiduMap mBaiduMap;
    Marker marker;
    boolean isFirstLoc = true; // 是否首次定位
    AlertDialog alert = null;
    AlertDialog.Builder builder = null;
    private TextView btn_show_menu;
    private TextView login_phone;
    private String state = null;//司机状态
    Toolbar toolbar;
    //Jpush相关
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;
    private static final String TAG = "JPush";
    private String phone;//已登录的司机电话
    private HttpUtils http = new HttpUtils();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_TAGS:
                    Log.d(TAG, "Set tags in handler.");
                    JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mTagsCallback);
                    break;

                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
                    break;
            }
        }
    };
    private Drawable drawable;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());//初始化百度地图
        setContentView(R.layout.activity_main);
        serverVersionNum();
        //设置状态栏透明
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//正常模式
        init();
        map();
        Intent intent = getIntent();
        sharedHelper = new SharedHelper(this);
        Map<String, String> passData = sharedHelper.readLoginMessage();
        if (passData.get("user") != null && !passData.get("user").equals("")) {
            phone = passData.get("user");
            login_phone.setText(phone.substring(0, 3) + "****" + phone.substring(7, phone.length()));
            setTag(phone);
        } else if (intent.getStringExtra("statePhone") != null) {
            phone = intent.getStringExtra("statePhone");
            login_phone.setText(phone.substring(0, 3) + "****" + phone.substring(7, phone.length()));
            setTag(phone);
        }
        state = "2";
        updateState();
        //十秒向服务器上传一次坐标
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStopped) {
                    if (phone != null) {
                        updateLocation();
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }

    /**
     * 设置状态按钮背景资源
     *
     * @param resID 图片ID
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBackground(int resID) {
        drawable = this.getResources().getDrawable(resID);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        btn_show_menu.setCompoundDrawables(null, drawable, null, null);
    }

    private volatile boolean isStopped = false;

    /**
     * 设置推送tag
     *
     * @param tag
     */
    private void setTag(String tag) {
        // 检查 tag 的有效性
        if (TextUtils.isEmpty(tag)) {
            Toast.makeText(MainActivity.this, R.string.error_tag_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        // ","隔开的多个 转换成 Set
        String[] sArray = tag.split(",");
        Set<String> tagSet = new LinkedHashSet<String>();
        for (String sTagItme : sArray) {
            if (!ExampleUtil.isValidTagAndAlias(sTagItme)) {
                Toast.makeText(MainActivity.this, R.string.error_tag_gs_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            tagSet.add(sTagItme);
        }

        //调用JPush API设置Tag
        handler.sendMessage(handler.obtainMessage(MSG_SET_TAGS, tagSet));

    }

    /**
     * 极光推送Alias回调
     */
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }

            ExampleUtil.showToast(logs, getApplicationContext());
        }

    };
    /**
     * 极光推送Tag回调
     */
    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }
        }

    };

    /**
     * 线程结束
     */
    public void stop() {
        isStopped = true;
    }

    /**
     * 初始化
     */
    private TextView locationText;
    //private Button start_order;
    private ImageView imageView;
    private TextView appoint_order_list;

    /**
     * 初始化
     */
    private void init() {
        appoint_order_list = (TextView) findViewById(R.id.appoint_order_list);
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("汴梁司机");
        login_phone = (TextView) findViewById(R.id.login_phone);
        btn_show_menu = (TextView) findViewById(R.id.btn_show_menu);
        btn_show_menu.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        locationText = (TextView) findViewById(R.id.location);
        mMapView = (MapView) findViewById(R.id.map_view);
        imageView.setOnClickListener(this);
        appoint_order_list.setOnClickListener(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setTitle("");
        login_phone.setText("登录/注册");
    }

    /**
     * 设置底图显示模式
     *
     * @param view
     */
    public void setMapMode(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.normal:
                if (checked) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
                break;
            case R.id.statellite:
                if (checked) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置是否显示交通图
     *
     * @param view
     */
    public void setTraffic(View view) {
        mBaiduMap.setTrafficEnabled(((CheckBox) view).isChecked());
    }

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 控制dialog显示若干秒后自动关闭
     *
     * @param duration 毫秒数
     */
    public void show(long duration) {
        //创建自动关闭任务
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                alert.dismiss();
            }
        };
        //新建调度任务
        executor.schedule(runner, duration, TimeUnit.MILLISECONDS);
        alert.show();
    }


    /**
     * 地图相关
     */
    private void map() {
        mBaiduMap = mMapView.getMap();
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap
                .setMyLocationConfigeration(new MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker));
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedLocationDescribe(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        //地图点击事件
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                if (marker != null) {
                    marker.remove();
                }

                return false;
            }
        });
        //地图状态变化
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus status) {
            }

            // 移动结束，在这里需要计算出中心点坐标
            @Override
            public void onMapStatusChangeFinish(MapStatus status) {
                searchMoveFinish(status);
            }

            @Override
            public void onMapStatusChange(MapStatus status) {

            }
        });
    }

    /**
     * 中心坐标
     *
     * @param status
     */
    private void searchMoveFinish(MapStatus status) {
        GeoCoder geoCoder = GeoCoder.newInstance();
        ReverseGeoCodeOption reverseCoder = new ReverseGeoCodeOption();
        reverseCoder.location(status.target);
        geoCoder.reverseGeoCode(reverseCoder);
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
                if (arg0 != null && arg0.getPoiList() != null) {
                    Log.e("====", "" + arg0.getLocation().latitude + "==" + arg0.getLocation().longitude + "==" + arg0.getAddressDetail().district + "" +
                            arg0.getAddressDetail().province + "" + "" + arg0.getAddressDetail().city + "" + arg0.getAddressDetail().street + arg0.getAddressDetail().streetNumber);
                    ll = new LatLng(arg0.getLocation().latitude, arg0.getLocation().longitude);
                } else {

                }
                if (arg0 == null || arg0.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult arg0) {
            }
        });

    }

    /**
     * 车辆状态
     *//*
    private void carState() {
        String url = Global.ip + "Taxic/ordersAction-orderByConR.action?receive_phone=" + phone;
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.i("---------", s);
                NowList ol = JSONObject.parseObject(s, NowList.class);

                switch (ol.getMsg()) {
                    case "0":
                        //for (int i = 0; i < ol.getContent().size(); i++) {
                        NowList.Content content = ol.getContent().get(0);
                        if (content.getState() == 4) {
                            Toast.makeText(MainActivity.this, "订单尚未完成，请完成订单后重试！", Toast.LENGTH_SHORT).show();

                        } else if (content.getState() == 5) {
                            state = "1";
                            updateState();
                            setBackground(R.mipmap.appiont_order);
                            Intent start_result = new Intent(MainActivity.this, DriverOrderActivity.class);
                            start_result.putExtra("statePhone", phone);
                            startActivityForResult(start_result, 10);
                        }
                        //  }
                        break;
                    case "101":
                        state = "1";
                        updateState();
                        setBackground(R.mipmap.appiont_order);
                        // start_order.setVisibility(View.VISIBLE);
                        break;
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(MainActivity.this, "网路连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private int taxiState = 0;

    @Override

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show_menu:

                switch (taxiState) {
                    case 0:
                        state = "1";
                        updateState();
                        setBackground(R.mipmap.appiont_order);
                        Intent start_result = new Intent(MainActivity.this, DriverOrderActivity.class);
                        start_result.putExtra("statePhone", phone);
                        startActivityForResult(start_result, 10);
                        Log.e("taxi5-------", taxiState + "");
                        taxiState = 1;
                        break;
                    case 1:
                        setBackground(R.mipmap.empty_car);
                        state = "2";
                        updateState();
                        taxiState = 0;
                        Log.e("taxi4-------", taxiState + "");
                        break;
                    default:
                        break;

                }
                break;
            case R.id.appoint_order_list:
                Intent intent = new Intent(this, AppointmentOrderListActivity.class);
                intent.putExtra("statePhone", phone);
                startActivity(intent);
                break;
            default:
                break;

        }
    }

    /**
     * 带返回值的intent
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == 11) {
            passenger_phone = data.getStringExtra("passengerPhone");
            Log.e("---phone", passenger_phone);
            state = "2";
            setBackground(R.mipmap.appiont_order);
            taxiState = 0;
            Log.e("taxi3-------", taxiState + "");
            updateState();
        }
    }

    /**
     * 更新坐标
     */
    private void updateLocation() {
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, Global.ip + "Taxic/driverAction-updateXY.action?phone=" + phone + "&longitude=" + pLongitude + "&latitude=" + pLatitude, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                UpdateLocation ul = JSONObject.parseObject(s, UpdateLocation.class);
                switch (ul.getMsg()) {
                    case "0":
                        if (ul.getCode().equals("0")) {
                            Log.i("----------", "司机坐标更新成功！");
                        } else {
                            Log.i("----------", "云端操作失败！");
                        }
                        break;
                    case "102":
                        Toast.makeText(MainActivity.this, "用户不存在！请重试！", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        Toast.makeText(MainActivity.this, "参数解析失败！请重试！", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    /**
     * 更新状态
     */
    private void updateState() {
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);

        String url = Global.ip + "Taxic/driverAction-updSta.action?phone=" + phone + "&state=" + state;
        Log.i("----------", url);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("----------", "司机状态更新成功");
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Log.i("----------", "司机状态更新失败");
                Toast.makeText(MainActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static String pLatitude;
    public static String pLongitude;

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(40).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            locationText.setText(location.getLocationDescribe());
            mBaiduMap.setMyLocationData(locData);
            pLatitude = location.getLatitude() + "";
            pLongitude = location.getLongitude() + "";
            /**
             * 是否第一次定位
             */
            if (isFirstLoc) {
                isFirstLoc = false;
                locationText.setText(location.getLocationDescribe());
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                pLatitude = location.getLatitude() + "";
                pLongitude = location.getLongitude() + "";
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        isForeground = true;
        super.onResume();
        //  queryState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        queryState();
    }

    @Override
    protected void onDestroy() {
        state = "2";
        updateState();
        setTag("0");
        stop();
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }


    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.sign_off) {

            Intent intent = new Intent(this, LoginActivity.class);

            startActivity(intent);
            finish();
        } else if (id == R.id.robbed_manage) {
            Intent intent = new Intent(this, RobbedAppointmentOrderActivity.class);
            intent.putExtra("statePhone", phone);
            startActivity(intent);
        } else if (id == R.id.now_manage) {
            Intent intent = new Intent(this, RealTimeListActivity.class);
            intent.putExtra("statePhone", phone);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            alert = null;
            builder = new AlertDialog.Builder(this);
            alert = builder.setIcon(R.mipmap.car).setTitle("更新").setMessage("已是最新版本！").create();
            alert.show();
            show(2000);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private long firstTime = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    state = "2";
                    updateState();
                    ExampleApplication.exit();
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // queryState();
    }

    /**
     * 查询司机状态
     */
    private void queryState() {
        String url = Global.ip + "Taxic/driverAction-chedstu.action?phone=" + phone;
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;

                QueryState qs = JSON.parseObject(s, QueryState.class);
                switch (qs.getMsg()) {
                    case "0":
                        if (qs.getDriver().equals("2")) {

                            setBackground(R.mipmap.empty_car);
                            taxiState = 0;
                            Log.e("taxi1-------", taxiState + "");
                        } else if (qs.getDriver().equals("1")) {
                            setBackground(R.mipmap.appiont_order);
                            taxiState = 1;
                            Log.e("taxi2-------", taxiState + "");
                        }

                        break;
                    case "102":
                        Toast.makeText(MainActivity.this, "司机不存在", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        Toast.makeText(MainActivity.this, "参数解析失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(MainActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 检查版本更新
     */
    public void checkVersion(String message) {
        // 判断本地版本是否小于服务器端的版本号
        Log.e("============", Global.localVersion + "" + Global.serverVersion);
        if (Global.localVersion < Global.serverVersion) {
            // 发现新版本，提示用户更新
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("软件升级")
                    .setMessage(message)
                    .setPositiveButton("更新",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // 开启更新服务UpdateService
                                    // 这里为了把update更好模块化，可以传一些updateService依赖的值
                                    // 如布局ID，资源ID，动态获取的标题,这里以app_name为例
                                    Intent updateIntent = new Intent(
                                            MainActivity.this,
                                            UpdateService.class);
                                    updateIntent.putExtra("titleId",
                                            R.string.app_name);
                                    startService(updateIntent);
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            });
            alert.create().show();
        } else {
            // 清理工作，略去
            cleanUpdateFile();
        }
    }

    /**
     * 查询服务器版本号
     */
    public void serverVersionNum() {

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET, Global.ip + "Taxic/driverAction-cheupdd.action", new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.e("--------s", s);
                VersionMessage vm = JSON.parseObject(s, VersionMessage.class);
                if (vm.getMsg() == 0) {
                    VersionMessage.Version version = vm.getVersion();

                    Global.serverVersion = version.getVersionCode();
                    Log.e("--------code", Global.serverVersion + "");
                    String message = version.getInformation();
                    try {
                        Global.localVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    checkVersion(message);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(MainActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 清理缓存的下载文件
     */
    private void cleanUpdateFile() {
        File updateFile = new File(Global.downloadDir, getResources()
                .getString(R.string.app_name) + ".apk");
        if (updateFile.exists()) {
            // 当不需要的时候，清除之前的下载文件，避免浪费用户空间
            updateFile.delete();
        }
    }
}
