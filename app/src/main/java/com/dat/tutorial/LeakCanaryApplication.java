package com.dat.tutorial;

import android.app.Application;


/**
 * <pre>
 * 内存溢出检测应用
 * Created by isanwenyu@163.com on 2016/12/9.
 * </pre>
 */
public class LeakCanaryApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

    }
}
