package com.rxjava.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.rxjava.R;
import com.rxjava.demo.create.ObservableObj;
import com.rxjava.demo.fanxing.Func1;
import com.rxjava.demo.fanxing.Functions;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ObservableObj obj = new ObservableObj();
        obj.combinelatest();
        /**
         * 1.创建被观察者
         * 2.创建观察者
         * 3.订阅
         */
    }
}
