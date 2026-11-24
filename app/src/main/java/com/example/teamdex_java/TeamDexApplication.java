package com.example.teamdex_java;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

public class TeamDexApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {

                    @Override
                    public void onActivityCreated(
                            Activity activity,
                            Bundle savedInstanceState
                    ) {
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {

                        ocultarBarrasSistema(activity);
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(
                            Activity activity,
                            Bundle outState
                    ) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                }
        );
    }

    private void ocultarBarrasSistema(Activity activity) {

        View decorView = activity.getWindow().getDecorView();

        int flags =
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        decorView.setSystemUiVisibility(flags);
    }
}
