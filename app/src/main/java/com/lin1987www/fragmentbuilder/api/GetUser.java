package com.lin1987www.fragmentbuilder.api;

import fix.java.util.concurrent.Duty;

/**
 * Created by Administrator on 2015/12/17.
 */
public class GetUser extends Duty<Void> {
    public String userName;

    private String result;

    public GetUser(String result) {
        this.result = result;
    }

    @Override
    public void doTask(Void context, Duty previousDuty) throws Throwable {
        Thread.sleep(500);
        userName = result;
    }
}
