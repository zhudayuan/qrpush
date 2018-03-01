package com.maywidehb.qrpush.push.service.impl;

import com.google.common.collect.Sets;
import com.maywidehb.qrpush.push.service.PushService;
import com.mpush.api.Constants;
import com.mpush.api.push.AckModel;
import com.mpush.api.push.MsgType;
import com.mpush.api.push.PushCallback;
import com.mpush.api.push.PushContext;
import com.mpush.api.push.PushMsg;
import com.mpush.api.push.PushResult;
import com.mpush.api.push.PushSender;
import com.mpush.api.router.ClientLocation;
import com.mpush.tools.Jsons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;



/**
 * Created by ohun on 16/9/15.
 *
 * @author ohun@live.cn (夜色)
 */
@Service
public class PushServiceImpl implements PushService{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicLong msgIdSeq = new AtomicLong(1);//TODO业务自己处理

    private PushSender mpusher;
    public PushServiceImpl(){

    }

    @Override
    public boolean sendBroadcast(List<String> tags, String condition, String message, PushCallback callback) throws Exception{
        return send(null,null, AckModel.AUTO_ACK, message,true,tags,
                condition,60000 ,callback);
    }


    @Override
    public boolean send(String userId, String message) throws Exception{
        if(StringUtils.isEmpty(userId)){
            throw new Exception("根据userId推送消息,userId不能为空");
        }
        return send(userId,null,AckModel.AUTO_ACK, message,false,null,
                null,60000 ,null);
    }
    @Override
    public boolean send(String userId ,String message, PushCallback callback) throws Exception{
        return send(userId,null,AckModel.AUTO_ACK, message,false,null,
                null,60000 ,callback);
    }

    @Override
    public boolean send(String userId,List<String> userIds ,String message, PushCallback callback) throws Exception{
        return send(userId,userIds,AckModel.AUTO_ACK, message,false,null,
                null,60000 ,callback);
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
     * @param callback 回电
     * @return boolean
     */
    private boolean send(String userId, List<String> userIds, AckModel ackModel,
                        String message, boolean Broadcast, List<String> tags,
                        String condition, int timeout, PushCallback callback){


        if(null == callback){
            callback= new PushCallback() {
                int retryCount = 0;

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
                    logger.warn("send msg offline,userId={},location={},content={}"
                            ,userId,clientLocation,message);
                }

                @Override
                public void onTimeout(String userId, ClientLocation clientLocation) {
                    if (retryCount++ > 3) {
                        logger.warn("send msg timeout 第"+ retryCount+"次,userId={},location={},content={}"
                                ,userId,clientLocation,message);
                    } else {
                        send( userId, userIds , ackModel, message,  Broadcast,  tags,
                                 condition, timeout , this) ;
                    }
                }
            };
        }

        PushMsg msg = PushMsg.build(MsgType.MESSAGE, message);
        msg.setMsgId("msgId_" + msgIdSeq.incrementAndGet());

        PushMsg pushMsg = PushMsg.build(MsgType.NOTIFICATION_AND_MESSAGE, message);
        pushMsg.setMsgId(Long.toString(msgIdSeq.incrementAndGet()));
        byte[] content = Jsons.toJson(pushMsg).getBytes(Constants.UTF_8);

        PushContext context = new PushContext(content)
                .setAckModel(ackModel)
                .setUserId(userId)
                .setBroadcast(Broadcast)
                .setTags(tags==null?null:Sets.newHashSet(tags))
                .setCondition(condition)
                .setUserIds(userIds)
                .setTimeout(timeout)
                .setCallback(callback);


        if(null ==mpusher){
            mpusher = PushSender.create();
            mpusher.start().join();
        }
        FutureTask<PushResult> future = mpusher.send(context);
        return true;
    }



}
