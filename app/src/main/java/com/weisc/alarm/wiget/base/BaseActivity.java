package com.weisc.alarm.wiget.base;

import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    protected BasePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResId());
        initializePresenter();
    }

    public abstract void initializePresenter();

    @LayoutRes
    public abstract int layoutResId();

    @MenuRes
    public int OptionsMenuResId() {
        return 0;
    }
    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        if (OptionsMenuResId() != 0) {
            getMenuInflater().inflate(OptionsMenuResId(), menu);
        }
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * @param systemUiVisibility look {@link View#SYSTEM_UI_FLAG_LIGHT_STATUS_BAR}
     */
    private void changeStatusBarColor(int systemUiVisibility) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        }
    }
}
