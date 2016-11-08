package com.zy.driverclient.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zy.driverclient.R;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.UserJson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by blurryface on 2016/9/26.
 */
public class SetPassActivity extends FatherActivity {
    private EditText password, name, carNum;
    private Button register;
    private TextView setPassWaring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_password_layout);
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("新用户注册");
        password = (EditText) findViewById(R.id.pass);
        name = (EditText) findViewById(R.id.name);
        carNum = (EditText) findViewById(R.id.carNum);
        register = (Button) findViewById(R.id.registerButton);
        setPassWaring = (TextView) findViewById(R.id.set_pass_warning);


        register.setEnabled(false);
        register.setTextColor(0xffffffff);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 8) {
                    setPassWaring.setText("密码不能少于8位！");
                } else {
                    setPassWaring.setText("");
                    register.setEnabled(true);
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String phone = intent.getStringExtra("phone");
                String pass = password.getText().toString();
                String sName = name.getText().toString();
                String carName = carNum.getText().toString();


                if (pass.isEmpty() || sName.isEmpty() || carName.isEmpty()) {
                    Toast.makeText(SetPassActivity.this, "姓名、密码或车牌号为空！", Toast.LENGTH_SHORT).show();
                } else if (pass.length() < 8 || pass.length() > 16) {
                    Toast.makeText(SetPassActivity.this, "密码长度为8位到16位，请修改后重试！", Toast.LENGTH_SHORT).show();
                } else if (sName.length() < 2 || sName.length() > 4) {
                    sName.length();
                    Toast.makeText(SetPassActivity.this, "姓名长度为2位到4位，请修改后重试！", Toast.LENGTH_SHORT).show();
                } else if (!carName.substring(0, 2).equalsIgnoreCase("豫B")) {
                    Toast.makeText(SetPassActivity.this, "车牌号必须以豫B开头,请修改后重试！", Toast.LENGTH_SHORT).show();
                } else if (phone.length() == 11 && isMobileNO(phone) == true) {
                    insertServer(phone, pass, sName, carName);
                } else {
                    Log.e("============1", isMobileNO(phone) + "");
                    Toast.makeText(SetPassActivity.this, "手机号码格式不正确，请重新输入后再试！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static boolean isMobileNO(String mobiles) {

        Pattern p = Pattern.compile("^(((13[0-9]{1})|(15[0-35-9]{1})|(17[0-9]{1})|(18[0-9]{1}))+\\d{8})$");

        Matcher m = p.matcher(mobiles);

        System.out.println(m.matches() + "---");

        return m.matches();

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
                Intent intent = new Intent(SetPassActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };
        //新建调度任务
        executor.schedule(runner, duration, TimeUnit.MILLISECONDS);
        alert.show();
    }

    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    private void insertServer(String phone, String pass, String mName, String carName) {
        HttpUtils http = new HttpUtils();
        String url = Global.ip+"Taxic/driverAction-register.action?phone=" + phone + "&pass=" + pass + "&name=" + mName + "&flapper=" + carName;

        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                UserJson uj = JSONObject.parseObject(s, UserJson.class);
                switch (uj.getMsg()) {
                    case "0":
                        alert = null;
                        builder = new AlertDialog.Builder(SetPassActivity.this);
                        alert = builder.setTitle("注册").setMessage("注册成功，2S后返回登录").create();
                        show(2000);
                        break;
                    case "101":
                        Toast.makeText(SetPassActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();
                        break;
                    case "102":
                        Toast.makeText(SetPassActivity.this, "用户已存在！", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        Toast.makeText(SetPassActivity.this, "参数解析失败！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(SetPassActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
