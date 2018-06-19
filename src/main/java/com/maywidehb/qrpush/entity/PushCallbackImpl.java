package com.maywidehb.qrpush.entity;

import com.maywidehb.qrpush.config.Logs;
import com.mpush.api.push.PushCallback;
import com.mpush.api.push.PushResult;

public class PushCallbackImpl implements PushCallback {
    public String info;
    public PushCallbackImpl(String msg){
        info = msg;
    }
    public PushCallbackImpl(){

    }

    @Override
    public void onResult(PushResult result) {
        Logs.QRPR.info("send msg {},userId={},info={}",result.getResultDesc(), result.userId,info);
    }


}
