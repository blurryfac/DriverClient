package com.zy.driverclient.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.zy.driverclient.tool.ToolAlert;
import com.zy.driverclient.tool.ToolSMS;
import com.zy.driverclient.tool.ToolString;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by blurryFace on 2016/9/19.
 */
public class RetrievePasswordActivity extends FatherActivity implements View.OnClickListener {
    private EditText et_phone, et_phone_code, newPass;
    private Button btn_gain_smscode, btn_validate;
    private TextView textColor;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private static int delay = 1 * 1000; // 1s
    private static int period = 1 * 1000; // 1s
    private static int count = 60;
    private static final int UPDATE_TEXTVIEW = 99;
    private Button next;
    private String mPhoneString = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retrieve_password_layout);
        initViews();
        ToolSMS.initSDK(this, ToolSMS.appkey, ToolSMS.appSecrect);

    }

    private void initViews() {
        // TODO Auto-generated method stub
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("修改密码");
        et_phone = (EditText) findViewById(R.id.et_phone2);
        et_phone_code = (EditText) findViewById(R.id.et_phone_code2);
        btn_gain_smscode = (Button) findViewById(R.id.btn_gain_smscode2);
        btn_validate = (Button) findViewById(R.id.btn_validate2);
        newPass = (EditText) findViewById(R.id.newPassNum);
        textColor= (TextView) findViewById(R.id.textColor);
        next = (Button) findViewById(R.id.next2);
        et_phone_code.setEnabled(false);
        btn_gain_smscode.setTextColor(0xffffffff);
        btn_validate.setTextColor(0xffffffff);
        btn_validate.setEnabled(false);
        next.setEnabled(false);
        next.setTextColor(0xffffffff);
        newPass.setEnabled(false);
        btn_gain_smscode.setText(String.format(
                getResources().getString(R.string.sms_timer), count));
        btn_gain_smscode.setOnClickListener(this);
        btn_validate.setOnClickListener(this);
        next.setOnClickListener(this);
    }

    /**
     * 启动Timer
     */
    private void startTimer() {

        stopTimer();

        // 输入框不可用，获取验证码按钮不可用
        et_phone.setEnabled(false);
        btn_gain_smscode.setEnabled(false);
        btn_validate.setEnabled(true);
        et_phone_code.setEnabled(true);
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = Message.obtain(handler, UPDATE_TEXTVIEW);
                    handler.sendMessage(message);
                    count--;
                }
            };
        }

        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, delay, period);
    }

    /**
     * 停止Timer
     */
    private void stopTimer() {

        btn_gain_smscode.setEnabled(true);
        et_phone.setEnabled(true);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

        count = 60;
        btn_gain_smscode.setText(String.format(
                getResources().getString(R.string.sms_timer), count));

    }

    /**
     * 更新倒计时
     */
    private void updateTextView() {

        // 停止Timer
        if (count == 0) {
            stopTimer();
            return;
        }

        if (count < 10) {
            btn_gain_smscode.setText(String.format(
                    getResources().getString(R.string.sms_timer),
                    "0" + String.valueOf(count)));
        } else {
            btn_gain_smscode.setText(String.format(
                    getResources().getString(R.string.sms_timer), count));
        }
    }

    public static boolean isMobileNO(String mobiles) {

        Pattern p = Pattern.compile("^(((13[0-9]{1})|(15[0-35-9]{1})|(17[0-9]{1})|(18[0-9]{1}))+\\d{8})$");

        Matcher m = p.matcher(mobiles);

        System.out.println(m.matches() + "---");

        return m.matches();

    }

    /***
     * 处理UI线程更新Handle
     **/
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case UPDATE_TEXTVIEW:
                    updateTextView();
                    break;

                default:
                    break;
            }
        }

        ;
    };

    private void sendServer(String phone, String password) {
        String url = Global.ip+"Taxic/driverAction-updpass.action?phone=" + phone + "&pass=" + password;
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        Log.e("----------", url);
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> info) {
                String s = info.result;
                Log.e("----------", s);
                UserJson uj = JSONObject.parseObject(s, UserJson.class);
                switch (uj.getMsg()) {
                    case "0":
                        alert = null;
                        builder = new AlertDialog.Builder(RetrievePasswordActivity.this);
                        alert = builder.setTitle("提示").setMessage("密码修改成功！").create();
                        show(2000);
                        break;
                    case "102":
                        Toast.makeText(RetrievePasswordActivity.this, "用户不存在！", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        Toast.makeText(RetrievePasswordActivity.this, "参数解析失败！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(RetrievePasswordActivity.this, "网络连接失败，请检查网络后重试！", Toast.LENGTH_SHORT).show();
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
                Intent intent =new Intent(RetrievePasswordActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        };
        //新建调度任务
        executor.schedule(runner, duration, TimeUnit.MILLISECONDS);
        alert.show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_validate2:
                if (ToolString.isNoBlankAndNoNull(et_phone_code.getText().toString())) {
                    ToolSMS.submitVerificationCode(mPhoneString,
                            et_phone_code.getText().toString(), new ToolSMS.IValidateSMSCode() {

                                @Override
                                public void onSucced() {
                                    // TODO Auto-generated method stub
                                    ToolAlert.toastShort(RetrievePasswordActivity.this, "验证成功");
                                    newPass.setEnabled(true);
                                    next.setEnabled(true);
                                    //释放监听器
                                    ToolSMS.release();
                                }

                                @Override
                                public void onFailed(Throwable e) {
                                    // TODO Auto-generated method stub
                                    ToolAlert.toastShort(RetrievePasswordActivity.this, "验证失败");
                                }
                            });
                } else {
                    Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_gain_smscode2:
                mPhoneString = et_phone.getText().toString();
                if (mPhoneString.length() == 11 && isMobileNO(mPhoneString) == true) {
                    if (ToolString.isNoBlankAndNoNull(et_phone.getText().toString())) {
                        ToolSMS.getVerificationCode(et_phone
                                .getText().toString());

                        startTimer();
                    }else {
                        Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("============1", isMobileNO(mPhoneString) + "");
                    Toast.makeText(RetrievePasswordActivity.this, "手机号码格式不正确，请重新输入后再试！", Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.next2:
                String newPassNum=newPass.getText().toString();
                Log.i("------------","---------");
                if(newPassNum.isEmpty()){
                    Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                }else if(newPassNum.length()<8){
                    Toast.makeText(this, "密码长度不的小于8位，请修改后重试！", Toast.LENGTH_SHORT).show();
                }else{
                    sendServer(mPhoneString,newPassNum);
                }

                break;

            default:
                break;
        }
    }
}
