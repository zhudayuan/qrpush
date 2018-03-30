package com.maywidehb.qrpush.service.push.impl;

import com.google.common.collect.Sets;
import com.maywidehb.qrpush.entity.RetResult;
import com.maywidehb.qrpush.service.push.PushManager;
import com.mpush.api.Constants;
import com.mpush.api.push.AckModel;
import com.mpush.api.push.PushCallback;
import com.mpush.api.push.PushContext;
import com.mpush.api.push.PushResult;
import com.mpush.api.push.PushSender;
import com.mpush.api.router.ClientLocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;




@Service
public class PushServiceImpl implements PushManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicLong msgIdSeq = new AtomicLong(1);

    private long startTime = 0;
    private long endTime = 0;

    @Resource
    private PushSender mpusher;
    public PushServiceImpl(){

    }

    @Override
    public RetResult sendBroadcast(List<String> tags, String condition, String message) throws Exception{
        return send(null,null, AckModel.NO_ACK, message,true,tags,
                condition,300000 ,null);
    }


    @Override
    public RetResult send(String userId, String message) throws Exception{
        if(StringUtils.isEmpty(userId)){
            throw new Exception("根据userId推送消息,userId不能为空");
        }
        return send(userId,null,AckModel.AUTO_ACK, message,false,null,
                null,60000 ,null);
    }


    @Override
    public RetResult send(List<String> userIds ,String message) throws Exception{
        return send(null,userIds,AckModel.AUTO_ACK, message,false,null,
                null,300000 ,null);
    }

    
    /**
     *推送优先级 userId > userIds > Broadcast(true)
     * @param userId  单用户
     * @param userIds  多用户
     * @param ackModel 客户端应答模式
     * @param message  推送内容
     * @param Broadcast 是否广播推送
     * @param tags  标签
     * @param condition "tags&&tags.indexOf('test')!=-1"
     * @param timeout 6000
     * @param callback 回调函数
     * @return boolean
     */
    private RetResult send(String userId, List<String> userIds, AckModel ackModel,
                        String message, boolean Broadcast, List<String> tags,
                        String condition, int timeout, PushCallback callback) throws Exception{
        callback = this.callBack( callback, message);
//        PushMsg pushMsg = PushMsg.build(MsgType.MESSAGE, message);
//        pushMsg.setMsgId(Long.toString(msgIdSeq.incrementAndGet()));
        byte[] content = message.getBytes(Constants.UTF_8);

        PushContext context = new PushContext(content)
                .setAckModel(ackModel)
                .setUserId(userId)
                .setUserIds(userIds)
                .setBroadcast(Broadcast)
                .setTags(tags==null?null:Sets.newHashSet(tags))
                .setCondition(condition)
                .setTimeout(timeout)
                .setCallback(callback);

        return send2(context, 2);
    }



    private RetResult send2(PushContext context, int num ) throws Exception{
        startTime = System.currentTimeMillis();

        if(null ==mpusher){
            mpusher = PushSender.create();
            mpusher.start().join();
        }
        FutureTask<PushResult> future = mpusher.send(context);
        PushResult futureResult = new PushResult(2);
        boolean flag = true;
        String res;

        while(flag){
            if(future.isDone()){
                futureResult = future.get();
                if(PushResult.CODE_TIMEOUT == futureResult.resultCode && num > 0){
                    num--;
                    send2(context, num);
                }
                flag = false;
            }
        }
        endTime = System.currentTimeMillis();
        RetResult ret=new RetResult();
        ret.setRetCode(futureResult.resultCode)
                .setCostTime(endTime - startTime)
                .setRetMsg(futureResult.getResultDesc())
                .setRetData(futureResult.getLocation())
                .setUserId(context.getUserId());
//                .setSedMsg(Jsons.fromJson(context.getContext(),Object.class));
//        System.out.println("====推送返回的结果是："+ Jsons.toJson(ret).toString());

        return ret;
    }

    private PushCallback callBack(PushCallback callback,String message){
        if(null == callback){
            callback= new PushCallback() {
                @Override
                public void onSuccess(String userId, ClientLocation clientLocation) {
                    logger.warn("send msg success,userId={},location={},content={}"
                            ,userId,clientLocation,message);
                }
                @Override
                public void onFailure(String userId, ClientLocation clientLocation) {
                    logger.warn("send msg failure,userId={},location={},content={}"
                            ,userId,clientLocation,message);
                }
                @Override
                public void onOffline(String userId, ClientLocation clientLocation) {
                    logger.warn("send msg offline,userId={}",userId);
                }
                @Override
                public void onTimeout(String userId, ClientLocation clientLocation) {
                    logger.warn("send msg timeout ,userId={}",userId);
                }
            };
        }

        return callback;
    }




}
