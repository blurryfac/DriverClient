package com.zy.driverclient.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zy.driverclient.ExampleApplication;

public class TimeView extends TextView implements Runnable {
 
    private int position;
 
    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        postDelayed(this, 1000);
    }
 
    public void setPosition(int position) {
        this.position = position;
    }
 
    @Override
    public void run() {
//        //String time= ExampleApplication.get(position, "time");
//
//        if(!"超时".equals(time)){
//            postDelayed(this, 1000);
//
//        }
    }
 
    public TimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
 
    public TimeView(Context context) {
        this(context, null);
    }
}
