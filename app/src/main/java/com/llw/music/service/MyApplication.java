package com.llw.music.service;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.multidex.MultiDexApplication;

import org.litepal.LitePal;

/**
 * 获取全局context*/
public class MyApplication extends MultiDexApplication {

    private static ActivityCollector activityManager;
    private static MyApplication application;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        //声明Activity管理
        LitePal.initialize(this);
        activityManager = new ActivityCollector();
        context = getApplicationContext();
        application = this;

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                ActivityCollector.setCurrentActivity(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    public static ActivityCollector getActivityManager() {
        return activityManager;
    }

    /**
     * 内容提供器
     * @return
     */
    public static Context getContext() {
        return context;
    }

    public static MyApplication getApplication() {
        return application;
    }
}
