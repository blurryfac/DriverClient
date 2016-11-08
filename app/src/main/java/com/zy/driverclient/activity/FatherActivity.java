package com.zy.driverclient.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.zy.driverclient.ExampleApplication;


/**
 * Created by blurryFace on 2016/8/21.
 */
public class FatherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExampleApplication.addActivity(this);
        super.onCreate(savedInstanceState);
    }

}
