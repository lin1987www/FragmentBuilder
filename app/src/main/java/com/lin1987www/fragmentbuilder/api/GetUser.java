package com.lin1987www.fragmentbuilder.api;

import fix.java.util.concurrent.Take;

/**
 * Created by Administrator on 2015/7/14.
 */
public class GetUser extends Take<GetUser> {
    public String userName;

    @Override
    public GetUser take() throws Throwable {
        Thread.sleep(500);
        userName = "John";
        return this;
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }
}
