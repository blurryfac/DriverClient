package com.zy.driverclient.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zy.driverclient.R;
import com.zy.driverclient.adapter.GuidePageAdapter;


public class GuidePageActivity extends Activity implements OnClickListener,
        OnPageChangeListener {
    private ViewPager vp;
    private GuidePageAdapter gpAdapter;
    private List<View> views;
    public static final int pics[] = {R.mipmap.car, R.mipmap.car,
            R.mipmap.car};
    private int currentIndex;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_page_layout);
        views = new ArrayList<View>();
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // ��ʼ������ͼƬ�б�
        for (int i = 0; i < pics.length; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(mParams);
            iv.setImageResource(pics[i]);
            views.add(iv);
        }
        vp = (ViewPager) findViewById(R.id.viewPager);
        // ��ʼ��Adapter
        gpAdapter = new GuidePageAdapter(views);
        vp.setAdapter(gpAdapter);
        // �󶨻ص�
        vp.setOnPageChangeListener(this);

        // ��ʼ���ײ�С��
        initDots();
    }

    private void setCurView(int position) {
        if (position < 0 || position >= pics.length) {
            return;
        }

        vp.setCurrentItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        if (arg0 == pics.length - 1) {
            SharedPreferences sp = getSharedPreferences("login",
                    Context.MODE_PRIVATE);
            sp.edit().putBoolean("isFirst", false).commit();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            finish();
        }
    }

    @Override
    public void onPageSelected(int arg0) {
        // TODO Auto-generated method stub
        setCurDot(arg0);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int position = (Integer) v.getTag();
        setCurView(position);
        setCurDot(position);
    }

    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        dots = new ImageView[pics.length];

        for (int i = 0; i < pics.length; i++) {
            dots[i] = (ImageView) ll.getChildAt(i);
            dots[i].setBackgroundResource(R.mipmap.car);
            dots[i].setOnClickListener(this);
            dots[i].setTag(i);
        }

        currentIndex = 0;
        dots[currentIndex].setBackgroundResource(R.mipmap.car);
    }

    private void setCurDot(int positon) {
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }

        dots[positon].setBackgroundResource(R.mipmap.car);
        dots[currentIndex].setBackgroundResource(R.mipmap.car);

        currentIndex = positon;
    }

}
