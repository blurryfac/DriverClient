package com.zy.driverclient.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.zy.driverclient.MainActivity;
import com.zy.driverclient.R;
import com.zy.driverclient.config.Global;
import com.zy.driverclient.model.UserJson;
import com.zy.driverclient.utils.ExampleUtil;
import com.zy.driverclient.utils.SharedHelper;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by blurryFace on 2016/8/9.
 */
public class LoginActivity extends InstrumentedActivity implements View.OnClickListener {
    private EditText phoneNum, passwordNum;
    private TextView newUser,getPass;
    private Button login_btn;
    private CheckBox checkBox;
    private SharedHelper sharedHelper;
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;
    private static final String TAG = "JPush";
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sharedHelper = new SharedHelper(this);
        init();
        Map<String, String> passData = sharedHelper.readMessage();
        if (passData.get("user") != null && !passData.get("user").equals("")) {
            phoneNum.setText(passData.get("user"));
            passwordNum.setText(passData.get("password"));
            checkBox.setChecked(true);
        }

        checkBoxState();
    }

    private void initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void init() {
        TextView toolbarTitle = (TextView) findViewById(R.id.title_tool);
        toolbarTitle.setText("登录");
        phoneNum = (EditText) findViewById(R.id.phone_num);
        passwordNum = (EditText) findViewById(R.id.pass_num);
        login_btn = (Button) findViewById(R.id.login_btn);
        checkBox = (CheckBox) findViewById(R.id.check);
        newUser = (TextView) findViewById(R.id.newUser);
        getPass = (TextView) findViewById(R.id.getPass);
        getPass.setOnClickListener(this);
        newUser.setOnClickListener(this);
        login_btn.setOnClickListener(this);
    }

    private void checkBoxState() {

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedHelper.saveMessage(phoneNum.getText().toString(), passwordNum.getText().toString());
                } else {
                    sharedHelper.clear();
                }
            }
        });
    }

    private void setTag() {
        // 检查 tag 的有效性
        String tag = phoneNum.getText().toString();
        if (TextUtils.isEmpty(tag)) {
            return;
        }

        // ","隔开的多个 转换成 Set
        String[] sArray = tag.split(",");
        Set<String> tagSet = new LinkedHashSet<String>();
        for (String sTagItme : sArray) {
            if (!ExampleUtil.isValidTagAndAlias(sTagItme)) {
                return;
            }
            tagSet.add(sTagItme);
        }

        //调用JPush API设置Tag
        handler.sendMessage(handler.obtainMessage(MSG_SET_TAGS, tagSet));

    }

    /**
     * 极光推送Alias回调
     */
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }

            //ExampleUtil.showToast(logs, getApplicationContext());
        }

    };
    /**
     * 极光推送Tag回调
     */
    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i(TAG, logs);
                    break;

                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i(TAG, logs);
                    if (ExampleUtil.isConnected(getApplicationContext())) {
                        handler.sendMessageDelayed(handler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                    } else {
                        Log.i(TAG, "No network");
                    }
                    break;

                default:
                    logs = "Failed with errorCode = " + code;
                    Log.e(TAG, logs);
            }

            ExampleUtil.showToast(logs, getApplicationContext());
        }

    };
    /**
     * 控制dialog显示若干秒后自动关闭
     *
     * @param duration 毫秒数
     */
    private ProgressDialog pd1 = null;

    /**
     * 登入服务器查询
     */
    private void loginServer() {
        String phone = phoneNum.getText().toString();
        String password = passwordNum.getText().toString();
        pd1 = new ProgressDialog(LoginActivity.this);
        //依次设置标题,内容,是否用取消按钮关闭,是否显示进度
        pd1.setTitle("登录");
        pd1.setMessage("登录中,请稍后...");
        pd1.setCancelable(true);
        //这里是设置进度条的风格,HORIZONTAL是水平进度条,SPINNER是圆形进度条
        pd1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd1.setIndeterminate(true);
        //调用show()方法将ProgressDialog显示出来
        pd1.show();
        HttpUtils http = new HttpUtils();
        http.configCurrentHttpCacheExpiry(1000);
        http.send(HttpRequest.HttpMethod.GET, Global.ip+"Taxic/driverAction-login.action?phone=" + phone + "&pass=" + password, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String s = responseInfo.result;
                UserJson uj = JSONObject.parseObject(s, UserJson.class);
                switch (uj.getMsg()) {
                    case "0":
                        setTag();
                        pd1.dismiss();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("statePhone", phoneNum.getText().toString());
                        startActivity(intent);
                        //setResult(11, intent);
                        finish();
                        break;
                    case "101":
                        pd1.dismiss();
                        Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case "102":
                        pd1.dismiss();
                        Toast.makeText(LoginActivity.this, "用户不存在！", Toast.LENGTH_SHORT).show();
                        break;
                    case "103":
                        pd1.dismiss();
                        Toast.makeText(LoginActivity.this, "参数解析失败！", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                pd1.dismiss();
                Toast.makeText(LoginActivity.this, "网络连接失败，请检查网络后重试！",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 正则表达式验证电话号码
     * @param mobiles phoneNumber
     * @return
     */
    public static boolean isMobileNO(String mobiles){

        Pattern p = Pattern.compile("^(((13[0-9]{1})|(15[0-35-9]{1})|(17[0-9]{1})|(18[0-9]{1}))+\\d{8})$");

        Matcher m = p.matcher(mobiles);

        System.out.println(m.matches() + "---");

        return m.matches();

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (checkBox.isChecked()) {
                    sharedHelper.saveMessage(phoneNum.getText().toString(), passwordNum.getText().toString());
                } else {
                    sharedHelper.clear();
                }
                String phone = phoneNum.getText().toString();
                String password = passwordNum.getText().toString();
                if (phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "手机号或密码为空！", Toast.LENGTH_SHORT).show();
                } else if (phone.length() == 11 && isMobileNO(phone) == true) {
                    loginServer();
                }else{
                    pd1.dismiss();
                    Toast.makeText(this, "手机号码格式不正确，请重新输入后再试！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.newUser:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.getPass:
                Intent in = new Intent(this, RetrievePasswordActivity.class);
                startActivity(in);
                finish();
                break;
            default:
                break;
        }
    }
}
