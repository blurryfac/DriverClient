package com.zy.driverclient;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * For developer startup JPush SDK
 * <p/>
 * 一般建议在自定义 Application 类里初始化。也可以在主 Activity 里。
 */
public class ExampleApplication extends Application {
    private static List<Map<String, String>> list1;
    private static final String TAG = "JPush";
    private static List<Activity> list = new LinkedList<>();

    @Override
    public void onCreate() {
        Log.d(TAG, "[ExampleApplication] onCreate");
        super.onCreate();
        StringBuffer param = new StringBuffer();
        param.append("appid=" + getString(R.string.app_id));
        param.append(",");

        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush
        JPushInterface.clearAllNotifications(this);
    }
    public static void set(List<Map<String, String>> l) {
        list1 = l;
    }



    public static void removeAll(){
        if (list1!=null)
            list1.clear();
        list1 = null;
    }
    public static void addActivity(Activity activity) {
        list.add(activity);
    }

    public static void exit() {
        for (Activity activity : list) {
            activity.finish();
        }
        System.exit(0);
    }

}