package com.zy.driverclient.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hellosliu.easyrecyclerview.LoadMoreRecylerView;
import com.hellosliu.easyrecyclerview.listener.OnRefreshListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zy.driverclient.R;
import com.zy.driverclient.adapter.RecyclerViewAdapter;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.AppointmentJson;
import com.zy.driverclient.model.OrderList;
import com.zy.driverclient.model.ShowOrderList;
import com.zy.driverclient.utils.SharedHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by blurryFace on 2016/8/19.
 */
public class AppointmentOrderListActivity extends FatherActivity {

    private List<ShowOrderList> list = new ArrayList<>();
    private RecyclerViewAdapter reAdapter;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private SharedHelper sharedHelper;
    private String phone;
    private int state = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list_layout);



        init();//初始化
        Intent intent = getIntent();
        sharedHelper = new SharedHelper(this);
        Map<String, String> passData = sharedHelper.readMessage();
        if (passData.get("user") != null && !passData.get("user").equals("")) {
            phone = passData.get("user");
        } else if (intent.getStringExtra("statePhone") != null) {
            phone = intent.getStringExtra("statePhone");
        }
        //获取订单列表
        builder =new AlertDialog.Builder(this);

        alert =builder.setMessage("加载中...").create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        getOrderList();
    }

    private LoadMoreRecylerView recyclerView;

    private void init() {
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("约车订单");
        //recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));
        recyclerView = (LoadMoreRecylerView) findViewById(R.id.orderList_one);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置加载更多监听
        recyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() { //上拉loading
                if (page < allPage) {
                    page++;
                    getOrderList();
                } else {
                    recyclerView.setDataEnd();
                }
            }

            @Override
            public void onReload() {  //网络异常时,点击重新获取数据

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

    private void order(int id) {
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET, Global.ip+"Taxic/tailoredAction-rob_order.action?receive_phone=" + phone + "&id=" + id, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.i("==========", s);
                AppointmentJson aj = JSONObject.parseObject(s, AppointmentJson.class);
                switch (aj.getMsg()) {
                    case "0":
                        alert.dismiss();
                        final AppointmentJson.Tailored tailored = aj.getTailored();
                        alert = null;
                        builder=new AlertDialog.Builder(AppointmentOrderListActivity.this);
                        alert = builder.setTitle("接单成功").setMessage("到预约订单管理中查看乘客信息？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AppointmentOrderListActivity.this, RobbedAppointmentOrderActivity.class);
                                intent.putExtra("statePhone",phone);
                                startActivity(intent);
                                finish();
                            }
                        }).create();
                        alert.show();
                        break;
                    case "101":
                        alert.dismiss();
                        alert = null;
                        builder = new AlertDialog.Builder(AppointmentOrderListActivity.this);
                        alert = builder.setMessage("订单已被抢").create();
                        show(1000);
                        break;
                    case "102":
                        alert.dismiss();
                        Toast.makeText(AppointmentOrderListActivity.this, " 订单不存在", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        alert.dismiss();
                        Toast.makeText(AppointmentOrderListActivity.this, "参数解析失败", Toast.LENGTH_SHORT).show();
                        break;
                    case "104":
                        alert.dismiss();
                        Toast.makeText(AppointmentOrderListActivity.this, "司机不存在", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    private int allPage = 0;
    private int page = 1;

    private void getOrderList() {


        String url = Global.ip+"Taxic/tailoredAction-tailoredByCons.action?&page=" + page;
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.i("---------", s);
                final OrderList ol = JSONObject.parseObject(s, OrderList.class);
                if (ol.getPage() != null) {
                    allPage = Integer.parseInt(ol.getPage());
                }
                switch (ol.getMsg()) {
                    case "0":
                        alert.dismiss();
                        for (int i = 0; i < ol.getContent().size(); i++) {
                            OrderList.Content content = ol.getContent().get(i);
                            ShowOrderList order = new ShowOrderList();
                            order.setAddress(content.getStart());
                            String date = content.getTime().substring(0, 10);
                            order.setStartDate(date);
                            String time = content.getTime().substring(10, content.getTime().length());
                            order.setStartTime(time);
                            order.setId(content.getId());
                            list.add(order);
                        }
                        reAdapter = new RecyclerViewAdapter(AppointmentOrderListActivity.this, list);

                        //设置布局管理器
                        //设置为垂直布局，这也是默认的
                        //设置Adapter
                        recyclerView.setAdapter(reAdapter);
                        //设置增加或删除条目的动画
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        reAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(int position) {
                                switch (state) {
                                    case 1:
                                        int mid = list.get(position).getId();
                                        OrderList.Content content = ol.getContent().get(position);
                                        String orderTime = content.getOrder_time();
                                        orderTime = orderTime.substring(0, 10) + " " + orderTime.substring(10, orderTime.length());
                                        String address = content.getStart();
                                        String end_add =content.getAddress();
                                        String phone =content.getPhone();
                                        int type = content.getType();
                                        String typeText=null;
                                        switch (type) {
                                            case 1:
                                                typeText="普通";
                                                break;
                                            case 2:
                                                typeText="豪华";
                                                break;
                                            default:
                                                break;
                                        }
                                        String seat = content.getSeat();
                                        showMessage(orderTime, address, typeText, seat,end_add,mid);
                                        break;
                                }
                            }

                            @Override
                            public void onLongClick(int position) {

                            }
                        });
                        recyclerView.onRefreshComplete();
                        break;
                    case "101":
                        alert.dismiss();
                        Toast.makeText(AppointmentOrderListActivity.this, "暂无可查询的订单！", Toast.LENGTH_SHORT).show();
                        break;

                }


            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }
    private View dialog_view;
    private TextView start_add_dia, start_time_dia, car_state, people_num,end_dia_appoint;
    private Button ok,no;
    private void showMessage(String orderTime, String address, String type, String seat,  String end_add, final int id) {
        alert = null;
        builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = AppointmentOrderListActivity.this.getLayoutInflater();
        dialog_view = inflater.inflate(R.layout.show_app_dialog, null, false);
        start_add_dia = (TextView) dialog_view.findViewById(R.id.start_add_dia_appoint);
        start_time_dia = (TextView) dialog_view.findViewById(R.id.start_time_dia_appoint);
        car_state = (TextView) dialog_view.findViewById(R.id.car_state_appoint);
        people_num = (TextView) dialog_view.findViewById(R.id.people_num_appoint);
        end_dia_appoint= (TextView) dialog_view.findViewById(R.id.end_dia_appoint);
        ok = (Button) dialog_view.findViewById(R.id.appoint_order);
        no = (Button) dialog_view.findViewById(R.id.cancel);
        start_add_dia.setText(address);
        start_time_dia.setText(orderTime);
        end_dia_appoint.setText(end_add);
        car_state.setText(type + "");
        people_num.setText(seat);
        builder.setView(dialog_view);
        builder.setCancelable(false);
        alert = builder.create();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                order(id);
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();
    }
}
