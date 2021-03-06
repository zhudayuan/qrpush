package com.maywidehb.qrpush.service.Impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.maywidehb.qrpush.config.Logs;
import com.maywidehb.qrpush.config.QrConstant;
import com.maywidehb.qrpush.entity.PushCallbackImpl;
import com.maywidehb.qrpush.entity.QrCodeTv2;
import com.maywidehb.qrpush.entity.Result;
import com.maywidehb.qrpush.service.QrManager;
import com.maywidehb.qrpush.service.push.PushManager;
import com.maywidehb.qrpush.utils.RedisUtil;
import com.mpush.api.push.PushResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QrCodeImpl  implements QrManager {

    @Resource
    private PushManager pushManager;

    /**
     *
     */
    @Override
    public Result pushCodeToClient(String cardid,String code0,String qrid,String serviceid) throws Exception {
        if(StringUtils.isBlank(cardid)||StringUtils.isBlank(code0)){
            throw new Exception("卡号和code不能为空");
        }
        boolean flag = true;
        int code = Integer.valueOf(code0);
//        //检查
//        if((code == 2||code == 3)&&StringUtils.isNotBlank(qrid)){
//            flag=true;
//        }else if (code==6&&StringUtils.isNotBlank(serviceid) ){
//            flag=true;
//        }else if(code==7){
//            flag=true;
//        }
        if((code == 2||code == 3)&&StringUtils.isBlank(qrid)){
            flag = false;
        }else if (code == 6 && StringUtils.isBlank(serviceid) ){
            flag = false;
        }

        if(!flag){
            throw new Exception("参数不正确,请检查");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("qrid",qrid);
        jsonObject.put("serviceid",serviceid);

        return push(cardid, jsonObject.toString());

    }

    @Override
    public Result pushQrList(List<String> jsonList) throws Exception {
        Result ret = new Result();
        PushResult pushResult;
        String userId;
        String errorMsg = "";
//        String serialno = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
//        serialno += ((int)(Math.random()*900)+100);
        int timeout = 20000;
        int num = 0;
        long sTime=System.currentTimeMillis();
        Statistics statistics = new Statistics();
        String qrInfo;
        String qrtime = null;
        for(String ss:jsonList){
            JSONObject qrs = JSON.parseObject(ss);
            String channelIds = qrs.getString("CHANNELID");
            for(int i=0; channelIds!=null && i<channelIds.split(",").length;i++){
                JSONObject qr = JSON.parseObject(ss);
                userId = qr.getString("LOGICDEVNO");
                qr.put("CHANNELID",channelIds.split(",")[i]);
                num++;
                try {
                    qrtime = qr.getString("QRTIME");
                    qr = dealQrCode(qr);
                    qrInfo = qr.getString("qrid");
                    pushManager.futurePush(userId,qr.toString(),timeout,new PushCallbackImpl(qrInfo) {
                        @Override
                        public void onResult(PushResult result) {
                            statistics.add(result.resultCode);
                            Logs.QRPR.info("send msg {},userId={},qrid={},msg={}",result.getResultDesc(), result.userId,this.info,null);
                        }
                    });
                }catch (Exception e){
                    statistics.add(2);
                    errorMsg = e.getMessage();
                    Logs.QRPR.info("send msg failure,userId={},qrid={},msg={}",userId,qr.getString("qrid"),e.getMessage());
                }
            }
        }
        int j = timeout/10;
        while(statistics.totalNum.get() < num && j>0){
            j--;
            Thread.sleep(10);
        }

        statistics.totalNum.set(num);
        JSONObject rt =  new JSONObject();
        rt.put("costTime",System.currentTimeMillis()-sTime);
        rt.put("result",statistics.toString());
        rt.put("cardNum",jsonList.size());
        rt.put("qrtime",qrtime);
        if(StringUtils.isNotBlank(errorMsg)){
            rt.put("errorMsg",errorMsg);
        }
        ret.setCode(0);
        ret.setMsg("success");
        ret.setData(rt);
        return ret;
    }

    @Override
    public Result pushQrList2(List<String> jsonList) throws Exception {
        Result ret = new Result();
        PushResult pushResult;
        String userId = "";
        String errorMsg = "";
//        String serialno = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
//        serialno += ((int)(Math.random()*900)+100);
        int timeout = 20000;
        int num = 0;
        long sTime=System.currentTimeMillis();
        Statistics statistics = new Statistics();
        String qrInfo="",qrTime="";
        JSONArray qrs = new JSONArray(),qrs1 = new JSONArray();
        JSONObject qr =new JSONObject(),qr1 =new JSONObject();

        for(String ss:jsonList){
            qrs = JSON.parseArray(ss);
            String [] channelIds = qrs.getJSONObject(0).getString("CHANNELID").split(",");
            for(int i=0; i<channelIds.length;i++){
                String channelID = channelIds[i];
                qrs1.clear();
                qr1.clear();
                num++;
                qrInfo = "";
                try {
                    if(i==0){
                         qr.clear();
                        qrs.getJSONObject(0).put("CHANNELID",channelID);
                        qr=dealQrCode0(qrs.getJSONObject(0));
                        userId = qr.getString("cardid");
                        qrTime = qrs.getJSONObject(0).getString("QRTIME");
                    }
                    for(int j=0;qr!=null&j<qrs.size();j++){
                        qrs.getJSONObject(j).put("CHANNELID",channelID);
                        qrs1.add(dealQrCode1(qr));
                        qrInfo += qrs1.getJSONObject(j).getString("qrid")+"~";
                    }

                    qr1.put("code", 1);
                    qr1.put("starttime",qr.get("starttime"));
                    qr1.put("banner",qrs1);
                    //第三方参数,如跳转到app
                    String param3 = qr.getJSONObject("qrscene").getString("param3");
                    if(param3!=null){
                        qr1.put(param3,qr.getJSONObject("qrscene"));
                    }
                    Logs.Console.debug(qr1.toString());
                    FutureTask future = pushManager.futurePush(userId,qr1.toString(),timeout,new PushCallbackImpl(qrInfo) {
                        @Override
                        public void onResult(PushResult result) {
                            statistics.add(result.resultCode);
                            Logs.QRPR.info("send msg {},userId={},qrid={},msg={}",result.getResultDesc(), result.userId,this.info,null);
                        }
                    });
                }catch (Exception e){
                    statistics.add(2);
                    errorMsg = e.getMessage();
                    Logs.QRPR.info("send msg failure,userId={},qrid={},msg={}",userId,qr.getString("qrid"),e.getMessage());
                    break;
                }
            }
        }
        int j = timeout/10;
        while(statistics.totalNum.get() < num && j>0){
            j--;
            Thread.sleep(10);
        }

        statistics.totalNum.set(num);
        JSONObject rt =  new JSONObject();
        rt.put("costTime",System.currentTimeMillis()-sTime);
        rt.put("result",statistics.toString());
        rt.put("cardNum",jsonList.size());
        rt.put("qrtime",qrTime);
        if(StringUtils.isNotBlank(errorMsg)){
            rt.put("errorMsg",errorMsg);
        }
        ret.setCode(0);
        ret.setMsg("success");
        ret.setData(rt);
        return ret;
    }
    private JSONObject dealQrCode0(JSONObject qr) throws Exception {
        JSONObject qrscene = new JSONObject();
        long startTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss").parse(qr.getString("QRTIME")).getTime();
        if(startTime < System.currentTimeMillis()){
            throw new Exception("QRTIME为"+qr.getString("QRTIME")+",小于当前时间");
        }
        String [] scene =qr.getString("QRURL").split("[?]");
        if(scene!=null && scene.length>1){
            String [] p =scene[1].split("&");
            for(int i=0;i < p.length ;i++){
                if(StringUtils.isNotBlank(p[i])&&p[i].contains("=")){
                    qrscene.put(p[i].split("=")[0], p[i].split("=")[1]);
                }
            }
        }
        if(!qrscene.containsKey("qrtype")){
            qrscene.put("qrtype", QrConstant.TCODE.COUPON_AD);
        }else if(QrConstant.TCODE.WC.equals(qrscene.getString("qrtype"))){
            //世界杯
            String url=RedisUtil.getKeyByDbidx("WC_"+qr.getString("cardid")+"_0",5);
            if(StringUtils.isBlank(url)){
                qr.put("QRURL", qr.getString("QRURL").split("[?]")[0]);
            }else{
                qr.put("QRURL", url);
            }
        }else if(QrConstant.TCODE.JUMP2APP.equals(qrscene.getString("qrtype"))){
            //世界杯
            qrscene.put("param3","jumptoapk");
        }
        qr.put("qrscene",qrscene);
        qrscene.put("cardid", qr.get("LOGICDEVNO"));
        qrscene.put("serviceid", qr.getLongValue("CHANNELID"));


        qr.put("cardid", qr.get("LOGICDEVNO"));
        qr.put("serviceid", qr.getLongValue("CHANNELID"));
        qr.put("workhours", qr.get("WORKTIMES"));
        qr.put("aftertime", qr.get("AFTERTIMES"));
        qr.put("COUNTDOWN", (0==qr.getInteger("COUNTDOWN")));

        qr.remove("WORKTIMES");
        qr.remove("LOGICDEVNO");
        qr.remove("CHANNELID");
        qr.remove("AFTERTIMES");
        qr.put( "starttime",startTime);
        qr.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        qr.put("code", 1);
        JSONObject jsonObject = getQrByAID(qr.getString("AID"));
        //转换二维码边距
        qr.put("QRHP", jsonObject.getIntValue("backhp")+qr.getIntValue("QRHP"));
        qr.put("QRWP", jsonObject.getIntValue("backwp")+qr.getIntValue("QRWP"));

        jsonObject  = mergeSameKeys(jsonObject,qr);
        return jsonObject;
    }
    private JSONObject dealQrCode1(JSONObject qr) throws Exception {
        JSONObject jsonObject= JSON.parseObject(qr.toString());
        Long qrid = RedisUtil.incr("QRPUT_QRID",2);
        jsonObject.put("qrid", qrid);
        jsonObject.put("qrurl", jsonObject.getString("qrurl").split("[?]")[0]+"?qrid="+qrid);

        int time = (int)((jsonObject.getLongValue("starttime")-System.currentTimeMillis()
                +jsonObject.getLongValue("aftertime")+jsonObject.getLong("workhours"))/1000+1800);
        RedisUtil.setex("QRID_"+qrid,time, jsonObject.getString("qrscene"), 5);
        //记录二维码内容qrid
        QrCodeTv2 qrCode=JSONObject.toJavaObject(jsonObject,QrCodeTv2.class);
        Logs.QRCODE.info(qrCode.toString());
        jsonObject.remove("qrscene");
        return jsonObject;
    }
    private JSONObject dealQrCode(JSONObject qr) throws Exception {
        JSONObject qrscene = new JSONObject();
        long startTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss").parse(qr.getString("QRTIME")).getTime();
        if(startTime < System.currentTimeMillis()){
            throw new Exception("QRTIME为"+qr.getString("QRTIME")+",小于当前时间");
        }
        String [] scene =qr.getString("QRURL").split("[?]");
        if(scene!=null && scene.length>1){
            String [] p =scene[1].split("&");
            for(int i=0;i < p.length ;i++){
                if(StringUtils.isNotBlank(p[i])&&p[i].contains("=")){
                    qrscene.put(p[i].split("=")[0], p[i].split("=")[1]);
                }
            }
        }
        qr.put("qrscene",qrscene.toString());
        qrscene.put("cardid", qr.get("LOGICDEVNO"));
        qrscene.put("serviceid", qr.getLongValue("CHANNELID"));

        Long qrid = RedisUtil.incr("QRPUT_QRID",2);
        qr.put("qrid", qrid);
        qr.put("QRURL", qr.getString("QRURL").split("[?]")[0]+"?qrid="+qrid);
        qr.put("cardid", qr.get("LOGICDEVNO"));
        qr.put("serviceid", qr.getLongValue("CHANNELID"));
        qr.put("workhours", qr.get("WORKTIMES"));
        qr.put("aftertime", qr.get("AFTERTIMES"));
        qr.put("COUNTDOWN", (0==qr.getInteger("COUNTDOWN")));

        qr.remove("WORKTIMES");
        qr.remove("LOGICDEVNO");
        qr.remove("CHANNELID");
        qr.remove("AFTERTIMES");
        qr.put( "starttime",startTime);
        qr.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        qr.put("code", 1);

        JSONObject jsonObject = getQrByAID(qr.getString("AID"));
        //转换二维码边距
        qr.put("QRHP", jsonObject.getIntValue("backhp")+qr.getIntValue("QRHP"));
        qr.put("QRWP", jsonObject.getIntValue("backwp")+qr.getIntValue("QRWP"));

        if(!qrscene.containsKey("qrtype")){
            qrscene.put("qrtype", QrConstant.TCODE.COUPON_AD);
        }else if(QrConstant.TCODE.WC.equals(qrscene.getString("qrtype"))){
            //世界杯
            String url=RedisUtil.getKeyByDbidx("WC_"+qr.getString("cardid")+"_0",5);
            if(StringUtils.isBlank(url)){
                qr.put("QRURL", qr.getString("QRURL").split("[?]")[0]);
            }else{
                qr.put("QRURL", url);
            }
        }

        jsonObject  = mergeSameKeys(jsonObject,qr);

        int time = (int)((startTime-System.currentTimeMillis()+qr.getLongValue("aftertime")+jsonObject.getLong("workhours"))/1000+1800);
        RedisUtil.setex("QRID_"+qrid,time, qrscene.toString(), 5);
        //记录二维码内容qrid
        QrCodeTv2 qrCode=JSONObject.toJavaObject(jsonObject,QrCodeTv2.class);
        jsonObject.remove("qrscene");
        Logs.QRCODE.info(qrCode.toString());
        return jsonObject;
    }

    private  JSONObject getQrByAID(String AID) throws Exception {

        String rule = "";

        if(QrConstant.PARAM.BI_AID_M.equals(AID)){
            rule = QrConstant.PARAM.QR_RULE_M;
        }else if(QrConstant.PARAM.BI_AID_R.equals(AID)){
            rule = QrConstant.PARAM.QR_RULE_R;
        }else{
            rule = RedisUtil.getKeyByDbidx("R_"+AID, 1);
        }
        if(StringUtils.isBlank(rule)){
            throw new Exception("根据广告位"+AID+",获取二维码规则失败!");
        }
        return JSON.parseObject(rule);
    }
    /**
     * json key相同值覆盖(忽略大小写)
     * @param json1 原
     * @param json2 取
     * @return json1
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public static JSONObject mergeSameKeys(JSONObject json1,JSONObject json2) throws JSONException {
        if(json2 == null || json2.entrySet().isEmpty()){
            return json1;
        }
        for (Map.Entry<String, Object> entry : json2.entrySet()) {
            json1.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return json1;
    }

    public Result pushReturn(String cardId,String serviceID,String tvTime,String qrId) throws Exception {
        if(StringUtils.isBlank(tvTime)||StringUtils.isBlank(serviceID)||StringUtils.isBlank(cardId)){
            throw new Exception("参数不能为空");
        }
        if(Long.parseLong(tvTime) > 0){
            String key = "V_"+cardId+"_"+serviceID;
            long time = RedisUtil.ttl(key,3);
            if(time == -2){
                RedisUtil.setex(key, (int)(Long.parseLong(tvTime)/1000), tvTime, 3);
            }else{
                RedisUtil.expire(key, (int)(time+(Long.parseLong(tvTime)/1000)),  3);
            }
        }else if(-1 == Long.parseLong(tvTime)){
            //订购频道,默认设置30分钟
            String key = "V_"+cardId+"_"+serviceID;
            RedisUtil.setex(key, 1800, tvTime, 3);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aftertime", Long.parseLong(tvTime));
        jsonObject.put("qrid", qrId);
        jsonObject.put("code", 3);
        return push(cardId, jsonObject.toString());

	}

	
	
	public Result push(String userId, String message) throws Exception {
		if(StringUtils.isBlank(userId)){
			throw new Exception("userId不能为空");
		}

        PushResult retResult = pushManager.send(userId,message,new PushCallbackImpl(message) {
            @Override
            public void onResult(PushResult result) {
                Logs.QRPR.info("send msg {},userId={},msg={}",result.getResultDesc(), result.userId,this.info);
            }
        });

        Result ret = new Result();
        if(null != retResult && 1 == retResult.resultCode){
            ret.setCode(0);
            ret.setMsg(retResult.getResultDesc());
        }else if(null != retResult){
            ret.setCode(1);
            ret.setMsg(retResult.getResultDesc());
            ret.setData(retResult);
        }
		return ret;
	}



    private static class Statistics {
        final AtomicInteger successNum = new AtomicInteger();
        final AtomicInteger failureNum = new AtomicInteger();
        final AtomicInteger offlineNum = new AtomicInteger();
        final AtomicInteger timeoutNum = new AtomicInteger();
        final AtomicInteger totalNum = new AtomicInteger();
        AtomicInteger[] counters = new AtomicInteger[]{successNum, failureNum, offlineNum, timeoutNum};

        private void add(int code) {
            totalNum.incrementAndGet();
            counters[code - 1].incrementAndGet();
        }

        @Override
        public String toString() {
            return "{" +
                    "totalNum=" + totalNum +
                    ", successNum=" + successNum +
                    ", offlineNum=" + offlineNum +
                    ", timeoutNum=" + timeoutNum +
                    ", failureNum=" + failureNum +
                    '}';
        }
    }
}
