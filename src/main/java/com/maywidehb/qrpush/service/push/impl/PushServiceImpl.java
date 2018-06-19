package com.maywidehb.qrpush.service.push.impl;

import com.maywidehb.qrpush.config.Logs;
import com.maywidehb.qrpush.service.push.PushManager;
import com.mpush.api.push.AckModel;
import com.mpush.api.push.PushCallback;
import com.mpush.api.push.PushContext;
import com.mpush.api.push.PushResult;
import com.mpush.api.push.PushSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;




@Service
public class PushServiceImpl implements PushManager {

    @Resource
    private PushSender mpusher;
    public PushServiceImpl(){
    }

    @Override
    public FutureTask sendBroadcast(List<String> tags, String condition, String message) throws Exception{
        return null;
    }


    @Override
    public PushResult send(String userId, String message, PushCallback callback) throws Exception{
        if(StringUtils.isEmpty(userId)){
            throw new Exception("根据userId推送消息,userId不能为空");
        }

        PushContext context = PushContext.build(message)
                .setAckModel(AckModel.AUTO_ACK)
                .setUserId(userId)
                .setTimeout(3000)
                .setCallback(callback);
        return SingleUserPush(context);

    }


    //同步推送,等待返回结果
    private PushResult SingleUserPush(PushContext context) throws Exception{
        if(null == mpusher){
            mpusher = PushSender.create();
            mpusher.start().join();
        }
        PushResult pushResult = new PushResult(2);
        FutureTask<PushResult> future = mpusher.send(context);
        //不是单用户推送,不返回推送结果
        if(context.getUserId() == null){
            return pushResult;
        }
        pushResult = future.get();
        int num = 3;
        while(PushResult.CODE_TIMEOUT == pushResult.resultCode && num > 0){
            if(context.getCallback() == null){
//                Logs.QRPR.info("send msg {},userId={},timeLine={}",
//                        pushResult.getResultDesc(), context.getUserId(), Arrays.toString(pushResult.timeLine));
                Logs.QRPR.info("send msg {},userId={},qrid={},msg={}",
                        pushResult.getResultDesc(), context.getUserId(),null,Arrays.toString(pushResult.timeLine));

            }
            Thread.sleep(100);
            context.setTimeout(context.getTimeout() + (3-num)*2000);
            num--;
            future = mpusher.send(context);
            pushResult = future.get();
        }
        if(context.getCallback() == null){
            Logs.QRPR.info("send msg {},userId={},qrid={},msg={}",
                    pushResult.getResultDesc(), context.getUserId(),null,null);
        }

        return pushResult;
    }

    @Override
    public FutureTask futurePush(String userId, String message, int timeout, PushCallback callBack) {

        if(null == mpusher){
            mpusher = PushSender.create();
            mpusher.start().join();
        }

        PushContext context = PushContext.build(message)
                .setAckModel(AckModel.NO_ACK)
                .setUserId(userId)
                .setTimeout(timeout)
                .setCallback(callBack);

        return mpusher.send(context);
    }

}
