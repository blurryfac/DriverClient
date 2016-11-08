package com.zy.driverclient.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.zy.driverclient.adapter.RecyclerRobbedAdapter;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.OrderList;
import com.zy.driverclient.model.ShowOrderList;
import com.zy.driverclient.utils.DividerItemDecoration;
import com.zy.driverclient.utils.SharedHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by blurryFace on 2016/8/24.
 */
public class RobbedAppointmentOrderActivity extends FatherActivity {
    private SharedHelper sharedHelper;
    private String phone;
    private LoadMoreRecylerView orderList;
    private List<ShowOrderList> list;
    private RecyclerRobbedAdapter adapter;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list_layout);
        Intent intent = getIntent();
        if (intent.getStringExtra("statePhone") != null) {
            phone = intent.getStringExtra("statePhone");
        }
        init();
        builder =new AlertDialog.Builder(this);

        alert =builder.setMessage("加载中...").create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        showOrderList();
    }

    private int allPage = 0;
    private int page = 1;

    private void init() {
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("已抢订单");
        orderList = (LoadMoreRecylerView) findViewById(R.id.orderList_one);
        orderList.setLayoutManager(new LinearLayoutManager(this));
//设置加载更多监听
        orderList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() { //上拉loading
                if (page < allPage) {
                    page++;
                    showOrderList();
                } else {
                    orderList.setDataEnd();
                }


            }

            @Override
            public void onReload() {  //网络异常时,点击重新获取数据

            }
        });
    }
    private void showOrderList() {
        list = new ArrayList<>();
        String url = Global.ip + "Taxic/tailoredAction-tailoredByConR.action?receive_phone=" + phone + "&page=" + page;
        Log.i("------------", "url");
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.e("------------", s);
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
                            Log.i("---------", content.getStart());
                            order.setPhone(content.getPhone());
                            order.setStartDate(date);
                            String time = content.getTime().substring(10, content.getTime().length());
                            order.setStartTime(time);
                            order.setId(content.getId());
                            list.add(order);
                        }
                        adapter = new RecyclerRobbedAdapter(RobbedAppointmentOrderActivity.this, list);
                        //设置布局管理器
                        //设置为垂直布局，这也是默认的
                        //设置Adapter
                        orderList.setAdapter(adapter);
                        adapter.setOnItemClickListener(new RecyclerRobbedAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(int position) {
                                OrderList.Content content = ol.getContent().get(position);
                                String orderTime = content.getOrder_time();
                                orderTime = orderTime.substring(0, 10) + " " + orderTime.substring(10, orderTime.length());
                                String address = content.getStart();
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
                                showMessage(orderTime, address, typeText, seat,phone);

                            }

                            @Override
                            public void onLongClick(int position) {

                            }
                        });
                        orderList.onRefreshComplete();
                        break;
                    case "101":
                        alert.dismiss();
                        Toast.makeText(RobbedAppointmentOrderActivity.this, "暂无可查询的订单！", Toast.LENGTH_SHORT).show();
                        break;

                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(RobbedAppointmentOrderActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View dialog_view;
    private TextView start_add_dia, start_time_dia, car_state, people_num;
    private Button ok,no;

    private void showMessage(String orderTime, String address, String type, String seat, final String phone) {
        alert = null;
        builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = RobbedAppointmentOrderActivity.this.getLayoutInflater();
        dialog_view = inflater.inflate(R.layout.show_rob_dialog, null, false);
        start_add_dia = (TextView) dialog_view.findViewById(R.id.start_add_dia_text);
        start_time_dia = (TextView) dialog_view.findViewById(R.id.start_time_dia_text);
        car_state = (TextView) dialog_view.findViewById(R.id.car_state_text);
        people_num = (TextView) dialog_view.findViewById(R.id.people_num_text);
        ok = (Button) dialog_view.findViewById(R.id.ok);
        no = (Button) dialog_view.findViewById(R.id.no);
        start_add_dia.setText(address);
        start_time_dia.setText(orderTime);
        car_state.setText(type + "");
        people_num.setText(seat);
        builder.setView(dialog_view);
        builder.setCancelable(false);
        alert = builder.create();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                Uri uri = Uri.parse("tel:" + phone);
                Intent intent = new Intent(Intent.ACTION_CALL, uri);
                startActivity(intent);
            }
        });
        alert.show();
    }
}
