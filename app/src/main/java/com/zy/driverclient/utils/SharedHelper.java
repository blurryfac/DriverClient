package com.zy.driverclient.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedHelper {
    private Context mContext;

    public SharedHelper() {
        super();
    }

    public SharedHelper(Context mContext) {
        super();
        this.mContext = mContext;
    }

    /**
     * 保存登陆信息
     * @param user 用户名
     * @param password 密码
     */
    public void saveMessage(String user, String password) {
        SharedPreferences sp = mContext.getSharedPreferences("mypass", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user", user);
        editor.putString("password", password);
        editor.commit();
    }

    /**
     * 读取登陆信息
     * @return Map数据
     */
    public Map<String, String> readMessage() {
        Map<String, String> data = new HashMap<String, String>();
        SharedPreferences sp = mContext.getSharedPreferences("mypass", Context.MODE_PRIVATE);
        data.put("user", sp.getString("user", ""));
        data.put("password", sp.getString("password", ""));
        return data;
    }

    /**
     * 清除SharedPreference
     */
    public void clear() {
        SharedPreferences sp = mContext.getSharedPreferences("mypass", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("user");
        editor.remove("password");
        editor.commit();
    }


}
