package com.maywidehb.qrpush.push.impl;

import com.google.common.collect.Sets;
import com.maywidehb.qrpush.push.PushManager;
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
    public String sendBroadcast(List<String> tags, String condition, String message) throws Exception{
        return send(null,null, AckModel.AUTO_ACK, message,true,tags,
                condition,60000 ,null);
    }


    @Override
    public String send(String userId, String message) throws Exception{
        if(StringUtils.isEmpty(userId)){
            throw new Exception("根据userId推送消息,userId不能为空");
        }
        return send(userId,null,AckModel.AUTO_ACK, message,false,null,
                null,6000 ,null);
    }


    @Override
    public String send(List<String> userIds ,String message) throws Exception{
        return send(null,userIds,AckModel.AUTO_ACK, message,false,null,
                null,60000 ,null);
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
    private String send(String userId, List<String> userIds, AckModel ackModel,
                        String message, boolean Broadcast, List<String> tags,
                        String condition, int timeout, PushCallback callback) throws Exception{
        callback = this.callBack( callback, message);
        PushMsg pushMsg = PushMsg.build(MsgType.MESSAGE, message);
        pushMsg.setMsgId(Long.toString(msgIdSeq.incrementAndGet()));
        byte[] content = Jsons.toJson(pushMsg).getBytes(Constants.UTF_8);

        PushContext context = new PushContext(content)
                .setAckModel(ackModel)
                .setUserId(userId)
                .setUserIds(userIds)
                .setBroadcast(Broadcast)
                .setTags(tags==null?null:Sets.newHashSet(tags))
                .setCondition(condition)
                .setTimeout(timeout)
                .setCallback(callback);

        return send2(context, 3);
    }



    private String send2(PushContext context,int num ) throws Exception{
        startTime = System.currentTimeMillis();

        if(null ==mpusher){
            mpusher = PushSender.create();
            mpusher.start().join();
        }
        FutureTask<PushResult> future = mpusher.send(context);
        PushResult futureResult =null;

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

        if(null != futureResult && futureResult.resultCode > -1){
            res = "Result{resultCode="+futureResult.resultCode+
                    ",resultCode=" + futureResult.getResultDesc() +
                    ", userId='" + futureResult.getUserId() + '\'' +
                    ", costTime=" + (endTime - startTime)+
                    ","+futureResult.getLocation()+"}";
        }else{
            res = "Result{resultCode=2,resultDesc=failure" +
                    ", userId='" + context.getUserId() + '\'' +
                    ", costTime=" + (endTime - startTime)+"}";
        }
        System.out.println("====推送返回的结果是："+res);

        return res;
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
                    logger.warn("send msg offline,userId={},location={},content={}"
                            ,userId,clientLocation,message);
                }
                @Override
                public void onTimeout(String userId, ClientLocation clientLocation) {
                    logger.warn("send msg timeout ,userId={},location={},content={}"
                            ,userId,clientLocation,message);
                }
            };
        }

        return callback;
    }




}
