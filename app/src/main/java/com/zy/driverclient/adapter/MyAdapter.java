package com.zy.driverclient.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zy.driverclient.R;
import com.zy.driverclient.model.DriverOrderList;

import java.util.List;

/**
 * Created by blurryface on 2016/9/30.
 */
public class MyAdapter extends BaseAdapter {
    //private List<Map<String, String>> list;
    private List<DriverOrderList> list;
    private Context mContext;

    public MyAdapter( List<DriverOrderList> list, Context mContext, MyClickListener mListener) {
        this.list = list;
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.order_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.id);
            viewHolder.positive = (TextView) convertView.findViewById(R.id.statePositive);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(list.get(position).getPlace());
        viewHolder.positive.setText("查看");
       // viewHolder.positive.setOnClickListener(mListener);
        viewHolder.positive.setTag(position);
        return convertView;
    }

    private MyClickListener mListener;

    class ViewHolder {
        TextView name;
        TextView positive;
    }

    /**
     * 用于回调的抽象类
     *
     * @author Ivan Xu
     *         2014-11-26
     */
    public static abstract class MyClickListener implements View.OnClickListener {
        /**
         * 基类的onClick方法
         */
        @Override
        public void onClick(View v) {
            myOnClick((Integer) v.getTag(), v);
        }

        public abstract void myOnClick(int position, View v);
    }
}
