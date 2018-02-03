package com.weisc.alarm.wiget.base;

/**
 * File Description:
 * Author:weisc
 * Create Date:18-2-3
 * Change List:
 */

public class BasePresenter<V extends BaseView> {

    private V mBaseView;

    public BasePresenter(V baseView) {
        this.mBaseView = baseView;
    }


}
