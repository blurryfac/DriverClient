package com.zy.driverclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zy.driverclient.R;
import com.zy.driverclient.model.ShowOrderList;

import java.util.List;

/**
 * Created by blurryFace on 2016/8/29.
 */
public class RecyclerRobbedAdapter extends RecyclerView.Adapter<RecyclerRobbedAdapter.ViewHolder>{
    private Context mContext;
    private List<ShowOrderList> list;
    private OnItemClickListener mOnItemClickListener;


    public RecyclerRobbedAdapter(Context mContext, List<ShowOrderList> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.robbed_layout, parent, false);
        ViewHolder holder= new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv2.setText(list.get(position).getAddress());
        holder.tv3.setText(list.get(position).getStartDate());
        holder.tv4.setText(list.get(position).getStartTime());
        if( mOnItemClickListener!= null){
            holder. itemView.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });

            holder. itemView.setOnLongClickListener( new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public interface OnItemClickListener{
        void onClick( int position);
        void onLongClick( int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this. mOnItemClickListener=onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv1, tv2, tv3,tv4;

        public ViewHolder(View itemView) {
            super(itemView);
            tv2= (TextView) itemView.findViewById(R.id.robbed_start_address_item);
            tv3= (TextView) itemView.findViewById(R.id.robbed_start_date);
            tv4= (TextView) itemView.findViewById(R.id.robbed_start_time);

        }
    }
}
