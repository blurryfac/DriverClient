package com.zy.driverclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.zy.driverclient.R;
import com.zy.driverclient.tool.ToolAlert;
import com.zy.driverclient.tool.ToolSMS;
import com.zy.driverclient.tool.ToolString;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by blurryFace on 2016/6/20.
 */
public class RegisterActivity extends Activity implements View.OnClickListener {
    private EditText et_phone, et_phone_code;
    private Button btn_gain_smscode, btn_validate;
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
        setContentView(R.layout.register_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initViews();
        ToolSMS.initSDK(this, ToolSMS.appkey, ToolSMS.appSecrect);
    }

    private void initViews() {
        // TODO Auto-generated method stub
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("新用户注册");
        et_phone = (EditText) findViewById(R.id.et_phone1);
        et_phone_code = (EditText) findViewById(R.id.et_phone_code1);
        btn_gain_smscode = (Button) findViewById(R.id.btn_gain_smscode1);
        btn_validate = (Button) findViewById(R.id.btn_validate1);
        next = (Button) findViewById(R.id.next);
        et_phone_code.setEnabled(false);
        btn_validate.setEnabled(false);
        next.setEnabled(false);
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_validate1:
                if (ToolString.isNoBlankAndNoNull(et_phone_code.getText().toString())) {
                    ToolSMS.submitVerificationCode(mPhoneString,
                            et_phone_code.getText().toString(), new ToolSMS.IValidateSMSCode() {

                                @Override
                                public void onSucced() {
                                    ToolAlert.toastShort(RegisterActivity.this, "验证成功");
                                    next.setEnabled(true);
                                    //释放监听器
                                    ToolSMS.release();
                                }

                                @Override
                                public void onFailed(Throwable e) {
                                    ToolAlert.toastShort(RegisterActivity.this, "验证失败");
                                }
                            });
                } else {
                    Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_gain_smscode1:
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
                    Toast.makeText(RegisterActivity.this, "手机号码格式不正确，请重新输入后再试！", Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.next:
                Intent intent = new Intent(this, SetPassActivity.class);
                intent.putExtra("phone", et_phone.getText().toString());
                Log.i("11111", et_phone.getText().toString());
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }
    }

    public static boolean isMobileNO(String mobiles) {

        Pattern p = Pattern.compile("^(((13[0-9]{1})|(15[0-35-9]{1})|(17[0-9]{1})|(18[0-9]{1}))+\\d{8})$");

        Matcher m = p.matcher(mobiles);

        System.out.println(m.matches() + "---");

        return m.matches();

    }
    /**
     * 从字符串中截取连续4位数字组合 ([0-9]{" + 4+ "})截取六位数字 进行前后断言不能出现数字 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的4位动态密码
     */
    public String getDynamicPassword(String str) {
        // 6是验证码的位数一般为六位
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 4 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            System.out.print(m.group());
            dynamicPassword = m.group();
        }

        return dynamicPassword;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToolSMS.release();
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
}
