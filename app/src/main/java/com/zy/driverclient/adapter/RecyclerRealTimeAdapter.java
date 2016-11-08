package com.zy.driverclient.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zy.driverclient.R;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.OrderJson;
import com.zy.driverclient.model.ShowOrderList;

import java.util.List;

/**
 * Created by blurryFace on 2016/8/29.
 */
public class RecyclerRealTimeAdapter extends RecyclerView.Adapter<RecyclerRealTimeAdapter.ViewHolder> {
    private Context mContext;
    private List<ShowOrderList> list;
    private OnItemClickListener mOnItemClickListener;


    public RecyclerRealTimeAdapter(Context mContext, List<ShowOrderList> list) {
        this.mContext = mContext;
        this.list = list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.now_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tv1.setText(list.get(position).getAddress());
        holder.tv3.setText(list.get(position).getStartTime());
        holder.tv2.setText(list.get(position).getStartDate());
        //holder.tv4.setText(list.get(position).getState());
//        if( holder.tv4.getText().equals("已完成")){
//            holder.tv4.setEnabled(false);
//            holder.tv4.setBackgroundResource(R.drawable.login_button);
//        }
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return false;
                }
            });
        }
       /* holder.tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("-------------click", "click---");
                AlertDialog alert = null;
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                alert = builder.setIcon(R.mipmap.car).setTitle("提示").setMessage("确定完成此次订单？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //sendStateSever(list.get(position).getId() + "", holder, position);

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
                alert.show();
            }
        });*/
    }

    private void sendStateSever(String id, final ViewHolder holder, final int position) {
        Log.e("-----phone----",id);
        String url = Global.ip+"Taxic/ordersAction-upd_order.action?id="+id+"&state=5";
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s =responseInfo.result;
                Log.i("-------------",s);
                OrderJson oj= JSON.parseObject(s,OrderJson.class);
                switch (oj.getMsg()){
                    case "0":
                        list.get(position).setState("已完成");
                        holder.tv4.setBackgroundResource(R.drawable.login_button);
                        holder.tv4.setText(list.get(position).getState());
                        holder.tv4.setEnabled(false);
                        break;
                    case "102":
                        Toast.makeText(mContext, "订单不存在！", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        Toast.makeText(mContext, "参数解析失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv1, tv2, tv3, tv4;

        public ViewHolder(View itemView) {
            super(itemView);
            tv1 = (TextView) itemView.findViewById(R.id.start_address_item);
            tv3 = (TextView) itemView.findViewById(R.id.start_time);
            tv2 = (TextView) itemView.findViewById(R.id.start_date);
            //tv4 = (TextView) itemView.findViewById(R.id.state);

        }
    }
}
