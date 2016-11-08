package com.zy.driverclient.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import com.zy.driverclient.adapter.RecyclerRealTimeAdapter;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.NowList;
import com.zy.driverclient.model.ShowOrderList;
import com.zy.driverclient.utils.DividerItemDecoration;
import com.zy.driverclient.utils.SharedHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by blurryFace on 2016/8/28.
 */
public class RealTimeListActivity extends FatherActivity {
    private LoadMoreRecylerView orderList;
    private List<ShowOrderList> list;
    private RecyclerRealTimeAdapter adapter;
    private String phone;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list_layout);
        init();
        Intent intent = getIntent();

        if (intent.getStringExtra("statePhone") != null) {
            phone = intent.getStringExtra("statePhone");
        }
        builder =new AlertDialog.Builder(this);

        alert =builder.setMessage("加载中...").create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
        getOrderList();
    }

    private int allPage = 0;

    private void init() {
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("实时订单");
        orderList = (LoadMoreRecylerView) findViewById(R.id.orderList_one);
        orderList.setLayoutManager(new LinearLayoutManager(this));
//设置加载更多监听
        orderList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() { //上拉loading
                if (page < allPage) {
                    page++;
                    getOrderList();
                } else {
                    orderList.setDataEnd();
                }


            }

            @Override
            public void onReload() {  //网络异常时,点击重新获取数据

            }
        });
//        View view = LayoutInflater.from(this).inflate(R.layout.now_list_item, null);
//        TextView tv = (TextView) view.findViewById(R.id.state);
//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(RealTimeListActivity.this, "nihao", Toast.LENGTH_SHORT).show();
//            }
//        });
    }




    private int page = 1;
    private void getOrderList() {
        list = new ArrayList<>();
        String url = Global.ip+"Taxic/ordersAction-orderByConR.action?receive_phone=" + phone + "&page=" + page;
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                Log.i("---------", s);
                NowList ol = JSONObject.parseObject(s, NowList.class);
                if (ol.getPage() != null) {
                    allPage = Integer.parseInt(ol.getPage());
                }
                switch (ol.getMsg()) {
                    case "0":
                        alert.dismiss();
                        for (int i = 0; i < ol.getContent().size(); i++) {
                            NowList.Content content = ol.getContent().get(i);
                            ShowOrderList order = new ShowOrderList();
                            String date = content.getTime().substring(0, 10);
                            if (content.getState() == 4) {

                                order.setState("未完成");

                            }else if(content.getState() == 5){

                                order.setState("已完成");
                            }
                            order.setAddress(content.getAddress());
                            order.setStartDate(date);
                            String time = content.getTime().substring(10, content.getTime().length());
                            order.setStartTime(time);
                            order.setPhone(content.getPhone());
                            order.setId(content.getId());
                            list.add(order);
                        }
                        adapter = new RecyclerRealTimeAdapter(RealTimeListActivity.this, list);
                        orderList.setAdapter(adapter);
                        adapter.setOnItemClickListener(new RecyclerRealTimeAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(int position) {
                                alert = null;
                                builder = new AlertDialog.Builder(RealTimeListActivity.this);
                                final String phone = list.get(position).getPhone();
                                alert = builder.setIcon(R.mipmap.car).setTitle("信息").setMessage("乘客电话：" + phone).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setPositiveButton("拨号", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse("tel:" + phone);
                                        Intent intent = new Intent(Intent.ACTION_CALL, uri);
                                        startActivity(intent);
                                    }
                                }).create();
                                alert.show();
                            }

                            @Override
                            public void onLongClick(int position) {

                            }
                        });
                        orderList.onRefreshComplete();
                        break;
                    case "101":
                        alert.dismiss();
                        Toast.makeText(RealTimeListActivity.this, "暂无订单可查询！", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(RealTimeListActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
