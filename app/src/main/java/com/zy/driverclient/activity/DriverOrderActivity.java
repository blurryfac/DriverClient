package com.zy.driverclient.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.utils.DistanceUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zy.driverclient.ExampleApplication;
import com.zy.driverclient.MainActivity;
import com.zy.driverclient.R;
import com.zy.driverclient.adapter.MyAdapter;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.DriverOrderList;
import com.zy.driverclient.model.OrderJson;
import com.zy.driverclient.model.QueryState;
import com.zy.driverclient.utils.ExampleUtil;
import com.zy.driverclient.utils.SharedHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by blurryFace on 2016/9/20.
 */
public class DriverOrderActivity extends FatherActivity {
    // public static List<Map<String, String>> list;
    private List<DriverOrderList> list;
    private ListView listView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x10:
                    Log.e("--------j", msg.arg1 + "---" + j);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private SharedHelper sharedHelper;
    private String phone;
    GeoCoder mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_order_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        registerMessageReceiver();
        init();
        Intent intent = getIntent();
        sharedHelper = new SharedHelper(this);
        Map<String, String> passData = sharedHelper.readMessage();
        if (passData.get("user") != null && !passData.get("user").equals("")) {
            phone = passData.get("user");
        } else if (intent.getStringExtra("statePhone") != null) {
            phone = intent.getStringExtra("statePhone");
        }

        if (intent.getStringExtra("extras") != null) {

            String extras = intent.getStringExtra("extras");
            //Map<String, String> map = new HashMap<>();
            // map.put("name", extras);
            // map.put("order", "抢单");
            DriverOrderList dol = new DriverOrderList();
            dol.setName(extras);
            String[] arr = extras.split(",");
            String a = arr[2].substring(2, arr[2].length() - 1);
            String b = arr[3].substring(2, arr[3].length() - 1);
            String address = arr[4].substring(4, arr[4].length() - 3);
            distance = twoPlaceDistace(a, b);
            dol.setAddress(address);
            dol.setPlace("距您" + (int) distance + "米乘客呼叫");
            dol.setOrder("查看");
            //list.add(map);
            list.add(0, dol);
            Log.i("=======", MainActivity.pLatitude + "_" + MainActivity.pLongitude);
            Log.e("=========distace", (int) distance + "");
            adapter.notifyDataSetChanged();
            if (list != null) {
                //start();
                startCountBack();
            }
        }
    }

    private Button takeOrder;
    private int mPisition;
    /**
     * 实现类，响应按钮点击事件
     */
    private MyAdapter.MyClickListener mListener = new MyAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View v) {
            mPisition = position;
            showOrderMessageDialog();

            Log.i("--------position", mPisition + "");

        }
    };

    private double twoPlaceDistace(String a, String b) {
        double pla2 = Double.parseDouble(a);
        double plo2 = Double.parseDouble(b);
        double pla1 = Double.parseDouble(MainActivity.pLatitude);
        double plo1 = Double.parseDouble(MainActivity.pLongitude);

        LatLng l1 = new LatLng(pla1, plo1);
        LatLng l2 = new LatLng(plo2, pla2);
        double distance = DistanceUtil.getDistance(l1, l2);
        Log.i("-----------dis", distance + "");

        return distance;

    }

    private int taxiState = 0;

    private void init() {
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("汴梁司机");
        listView = (ListView) findViewById(R.id.listview_order);
        takeOrder = (Button) findViewById(R.id.takeOrder);
        list = new ArrayList<>();
        // 数据拿到开始计时
        adapter = new MyAdapter(list, this, mListener);
        listView.setAdapter(adapter);
        takeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (taxiState) {
                    case 0:
                        state = "2";
                        updateState();
                        takeOrder.setText("状态：有客");
                        takeOrder.setBackgroundResource(R.drawable.state_bg_full);
                        taxiState = 1;
                        break;
                    case 1:
                        takeOrder.setText("状态：空车");
                        takeOrder.setBackgroundResource(R.drawable.state_bg_empty);

                        state = "1";
                        updateState();
                        taxiState = 0;
                        break;
                    default:
                        break;

                }
            }
        });

    }

    private String state = "1";//司机状态
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.jpushdemo.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_EXTRAS = "extras";


    /**
     * 注册广播
     */
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        registerReceiver(mMessageReceiver, filter);
    }

    private double distance;

    /**
     * 实现广播内部类
     */
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                String extras = intent.getStringExtra(KEY_EXTRAS);
                if (!ExampleUtil.isEmpty(extras)) {
//                    Map<String, String> map = new HashMap<>();
//                    map.put("name", extras);
//                    map.put("order", "抢单");
//                    list.add(map);
                    Log.i("----------", extras);
                    DriverOrderList dol = new DriverOrderList();
                    dol.setName(extras);
                    String[] arr = extras.split(",");
                    String a = arr[2].substring(2, arr[2].length() - 1);
                    String b = arr[3].substring(2, arr[3].length() - 1);
                    distance = twoPlaceDistace(a, b);
                    String address = arr[4].substring(4, arr[4].length() - 3);
                    dol.setAddress(address);
                    dol.setPlace("距您" + (int) distance + "米乘客呼叫");
                    dol.setOrder("查看");
                    dol.setOrder("抢单");
                    list.add(0, dol);
                    adapter.notifyDataSetChanged();
                    if (list != null) {
                        Log.i("-----------", "0000000000000");
                        startCountBack();
                        //start();
                    }
                }
            }

        }
    }


    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    /**
     * 抢单
     */
    private View view_custom;
    private double mLongitude;
    private double mLatitude;

    private void showOrder(String id) {
        HttpUtils http = new HttpUtils();
        String url = Global.ip + "Taxic/ordersAction-rob_order.action?receive_phone=" + phone + "&id=" + id;
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                OrderJson oj = JSONObject.parseObject(s, OrderJson.class);
                Log.i("-------", oj.toString());
                switch (oj.getMsg()) {
                    case "0":
                        imgState = true;
                        state = "2";
                        updateState();
                        OrderJson.Orders order = oj.getOrders();
                        final String passenger_phone = order.getPhone();
                        mLongitude = order.getLongitude();
                        mLatitude = order.getLatitude();
                        showOrderSuccess(passenger_phone);
                        list.removeAll(list);
                        adapter.notifyDataSetChanged();
                        break;
                    case "101":
//                        imgState=false;
//                        alert = null;
//                        builder = new AlertDialog.Builder(DriverOrderActivity.this);
//                        alert = builder.setMessage("订单已被抢").create();
//                        show(2000);
                        alert.dismiss();
                        showOrderFail();
                        list.remove(mPisition);
                        adapter.notifyDataSetChanged();
                        if (list == null || list.isEmpty()) {
                            timer.cancel();
                        }
                        break;
                    case "102":
                        alert.dismiss();
                        Toast.makeText(DriverOrderActivity.this, " 订单不存在", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        alert.dismiss();
                        Toast.makeText(DriverOrderActivity.this, "参数解析失败", Toast.LENGTH_SHORT).show();
                        break;
                    case "104":
                        alert.dismiss();
                        Toast.makeText(DriverOrderActivity.this, "司机不存在", Toast.LENGTH_SHORT).show();
                        break;
                }
            }


            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(DriverOrderActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Button deterMine;
    private Button backList;
    private TextView passenger_distance;
    private TextView passenger_location;
    private boolean imgState;


    private void showOrderMessageDialog() {
        alert = null;
        builder = new AlertDialog.Builder(DriverOrderActivity.this);
        //加载自定义的那个View,同时设置下
        final LayoutInflater inflater = DriverOrderActivity.this.getLayoutInflater();
        view_custom = inflater.inflate(R.layout.order_message_dialog, null, false);
        deterMine = (Button) view_custom.findViewById(R.id.deterMine);
        backList = (Button) view_custom.findViewById(R.id.backList);
        passenger_distance = (TextView) view_custom.findViewById(R.id.passenger_distance_text);
        passenger_location = (TextView) view_custom.findViewById(R.id.passenger_location_text);
        passenger_distance.setText((int) distance + "米");
        Log.e("-----------mPis", mPisition + "");
        Log.e("-----------mP", list.size() + "---list");
        passenger_location.setText(list.get(mPisition).getAddress());
        builder.setView(view_custom);
        builder.setCancelable(false);
        alert = builder.create();
        deterMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                String msg = list.get(mPisition).getName();
                String[] arr = msg.split(",");
                String id = arr[0].substring(15, arr[0].length());
                showOrder(id);
            }
        });
        backList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();

    }

    private View dialog_success, dialog_fail;
    private Button talk_pa, back_map, back_list_fail;
    private ImageView img;

    private void showOrderSuccess(final String passenger_phone) {
        alert = null;
        builder = new AlertDialog.Builder(DriverOrderActivity.this);
        final LayoutInflater inflater = DriverOrderActivity.this.getLayoutInflater();
        dialog_success = inflater.inflate(R.layout.order_success_dialog, null, false);
        talk_pa = (Button) dialog_success.findViewById(R.id.talk_pa);
        back_map = (Button) dialog_success.findViewById(R.id.back_map);
        builder.setView(dialog_success);
        builder.setCancelable(false);
        alert = builder.create();
        talk_pa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                takeOrder.setText("有客");
                state = "2";
                updateState();
                takeOrder.setBackgroundResource(R.drawable.state_bg_full);
                Uri uri = Uri.parse("tel:" + passenger_phone);
                Intent intent = new Intent(Intent.ACTION_CALL, uri);
                startActivity(intent);
            }
        });
        back_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                alert = null;
                builder = new AlertDialog.Builder(DriverOrderActivity.this);
                alert = builder.setMessage("请稍候...").create();
                Runnable runner = new Runnable() {
                    @Override
                    public void run() {
                        alert.dismiss();
                        queryState(passenger_phone);

                    }
                };
                //新建调度任务
                executor.schedule(runner, 3000, TimeUnit.MILLISECONDS);
                alert.show();
            }
        });
        alert.show();
    }

    private void showOrderFail() {
        alert = null;
        builder = new AlertDialog.Builder(DriverOrderActivity.this);
        final LayoutInflater inflater = DriverOrderActivity.this.getLayoutInflater();
        dialog_fail = inflater.inflate(R.layout.order_fail_dialog, null, false);
        back_list_fail = (Button) dialog_fail.findViewById(R.id.back_list_sec);
        builder.setView(dialog_fail);
        builder.setCancelable(false);
        alert = builder.create();
        back_list_fail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();
    }

    private void queryState(final String passenger_phone) {
        HttpUtils http = new HttpUtils();
        String url = Global.ip + "Taxic/driverAction-chedstu.action?phone=" + phone;
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.i("--------------", s);
                QueryState qs = JSON.parseObject(s, QueryState.class);
                switch (qs.getMsg()) {
                    case "0":
                        Intent intent = new Intent(DriverOrderActivity.this, MainActivity.class);
                        intent.putExtra("passengerPhone", passenger_phone);
                        Log.i("--------1--", passenger_phone);
                        setResult(11, intent);
                        finish();
                        break;
                    case "102":
                        Toast.makeText(DriverOrderActivity.this, "司机不存在", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        Toast.makeText(DriverOrderActivity.this, "参数解析失败", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(DriverOrderActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 更新状态
     */

    private void updateState() {
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, Global.ip + "Taxic/driverAction-updSta.action?phone=" + phone + "&state=" + state, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("---------1-", responseInfo.result);
                Log.i("----------", "司机状态更新成功");
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(DriverOrderActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
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

    MyAdapter adapter;
    int j = 0;
    private volatile boolean isStopped = false;
    private Timer timer;
    private TimerTask timerTask;

    protected void startCountBack() {

        timer = new Timer();
        timerTask = new TimerTask() {
            int count = 10;

            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.arg1 = count;
                handler.sendMessage(msg);
                count--;
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    public void start() {


        new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 20; i >= 0; i--) {
                    try {
                        Thread.sleep(1000);
                        Log.e("--------------0", i + "---" + j);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (list != null && !list.isEmpty()) {
                    list.remove(0);
                    Message msg = Message.obtain();
                    msg.what = 0x10;
                    handler.sendMessage(msg);
                }
            }
        }).start();

    }


    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    public static boolean isForeground = false;

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        list = null;
        super.onDestroy();
    }
}
