package com.reactnativenavigation.controllers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.reactnativenavigation.NavigationApplication;
import com.reactnativenavigation.react.*;
import com.reactnativenavigation.utils.CompatUtils;

public abstract class SplashActivity extends AppCompatActivity {
    public static boolean isResumed = false;

    public static void start(Activity activity) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (intent == null) return;
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LaunchArgs.instance.set(getIntent());
        setSplashLayout();
        IntentDataHandler.saveIntentData(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        if (ReactDevPermission.shouldAskPermission()) {
            ReactDevPermission.askPermission(this);
            return;
        }

        checkJSInitializationAndLaunchApp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    protected void checkJSInitializationAndLaunchApp() {
        if (NavigationApplication.instance.getReactGateway().hasStartedCreatingContext()) {
            if (CompatUtils.isSplashOpenedOverNavigationActivity(this, getIntent())) {
                finish();
                return;
            }
            NavigationApplication.instance.getEventEmitter().sendAppLaunchedEvent();
            if (NavigationApplication.instance.clearHostOnActivityDestroy()) {
                overridePendingTransition(0, 0);
                finish();
            }
            return;
        }

        if (NavigationApplication.instance.isReactContextInitialized()) {
            NavigationApplication.instance.getEventEmitter().sendAppLaunchedEvent();
            return;
        }

        NavigationApplication.instance.startReactContextOnceInBackgroundAndExecuteJS();
    }

    private void setSplashLayout() {
        final int splashLayout = getSplashLayout();
        if (splashLayout > 0) {
            setContentView(splashLayout);
        } else {
            setContentView(createSplashLayout());
        }
    }

    /**
     * @return xml layout res id
     */
    @LayoutRes
    public int getSplashLayout() {
        return 0;
    }

    /**
     * @return the layout you would like to show while react's js context loads
     */
    public View createSplashLayout() {
        View view = new View(this);
        view.setBackgroundColor(Color.WHITE);
        return view;
    }
}
