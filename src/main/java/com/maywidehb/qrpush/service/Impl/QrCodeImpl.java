package com.maywidehb.qrpush.service.Impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.maywidehb.qrpush.config.Logs;
import com.maywidehb.qrpush.config.QrConstant;
import com.maywidehb.qrpush.entity.Result;
import com.maywidehb.qrpush.entity.RetResult;
import com.maywidehb.qrpush.service.QrManager;
import com.maywidehb.qrpush.service.push.PushManager;
import com.maywidehb.qrpush.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class QrCodeImpl  implements QrManager {

    @Resource
    private PushManager pushManager;

    private RetResult result=new RetResult();

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public Result pushCodeToClient(String cardid,String code0,String qrid,String serviceid) throws Exception {
        if(StringUtils.isBlank(cardid)||StringUtils.isBlank(code0)){
            throw new Exception("卡号和code不能为空");
        }
        boolean flag = false;
        int code = Integer.valueOf(code0);
        //检查
        if((code == 2||code == 3)&&StringUtils.isNotBlank(qrid)){
            flag=true;
        }else if (code==6&&StringUtils.isNotBlank(serviceid) ){
            flag=true;
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
        List<String> list = new ArrayList<String>();
        int successNum = 0;
        int failureNum = 0;
        String cardid = null;
        long stime=System.currentTimeMillis();
        for(String ss:jsonList){
            JSONObject qrs = JSON.parseObject(ss);
            String channelIds = qrs.getString("CHANNELID");
            for(int i=0; channelIds!=null && i<channelIds.split(",").length;i++){
                JSONObject qr = JSON.parseObject(ss);
                cardid = qr.getString("LOGICDEVNO");
                qr.put("CHANNELID",channelIds.split(",")[i]);
                try {
                    qr = dealQrCode(qr);
                    ret = push(qr.getString("cardid"), qr.toString());
                }catch (Exception e){
                    ret.setCode(1);
                    ret.setMsg(e.getMessage());
                }

                if(0 == ret.getCode()){
                    successNum++;
                }else{
                    failureNum++;
                }
                list.add("&cardid="+cardid+"&code="+ret.getCode()+"&msg="+ret.getMsg());
            }
        }
        long etime=System.currentTimeMillis();


        JSONObject rt =  new JSONObject();
        rt.put("successNum",successNum);
        rt.put("failureNum",failureNum);
        rt.put("costTime",etime-stime);

        rt.put("result",list);
        ret.setCode(0);
        ret.setMsg("success");
        ret.setData(rt);
        return ret;
    }

    public JSONObject dealQrCode(JSONObject qr) throws Exception {
        JSONObject qrscene = new JSONObject();
        long startTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss").parse(qr.getString("QRTIME")).getTime();
        if(startTime < System.currentTimeMillis()){
            throw new Exception("此二维码展示时间为:"+qr.getString("QRTIME")+",小于当前时间");
        }
        String [] scene =qr.getString("QRURL").split("[?]");
        if(scene!=null && scene.length>1){
            String [] p =scene[1].split("&");
            for(int i=0;i < p.length ;i++){
                if(StringUtils.isNotBlank(p[i])&&p[i].contains("=")){
                    qr.put(p[i].split("=")[0], p[i].split("=")[1]);
                    qrscene.put(p[i].split("=")[0], p[i].split("=")[1]);
                }
            }
        }
        qrscene.put("cardid", qr.get("LOGICDEVNO"));
        qrscene.put("serviceid", qr.getLongValue("CHANNELID"));

        Long qrid = RedisUtil.incr("QRPUT_QRID",2);
        qr.put("qrid", qrid);
        qr.put("QRURL", qr.getString("QRURL").split("[?]")[0]+"?qrid="+qrid);
        qr.put("cardid", qr.get("LOGICDEVNO"));
        qr.put("serviceid", qr.getLongValue("CHANNELID"));
        qr.put("workhours", qr.get("WORKTIMES"));
        qr.put("aftertime", qr.get("AFTERTIMES"));
        qr.put("COUNTDOWN", (1==qr.getInteger("COUNTDOWN"))?true:false);

        qr.remove("WORKTIMES");
        qr.remove("LOGICDEVNO");
        qr.remove("CHANNELID");
        qr.remove("AFTERTIMES");

        qr.put( "startTime",startTime);
        qr.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        qr.put("code", 1);


        if(!qr.containsKey("qrtype")){
            qrscene.put("qrtype", QrConstant.TCODE.COUPON_AD);
            qr.put("qrtype",QrConstant.TCODE.COUPON_AD) ;
        }

        JSONObject jsonObject = getQrByAID(qr.getString("AID"));
        //转换二维码边距
        qr.put("QRHP", jsonObject.getIntValue("backhp")+qr.getIntValue("QRHP"));
        qr.put("QRWP", jsonObject.getIntValue("backwp")+qr.getIntValue("QRWP"));

        jsonObject  = mergeSameKeys(jsonObject,qr);

        int time = (int)((startTime-System.currentTimeMillis()+qr.getLongValue("aftertime")+jsonObject.getLong("workhours"))/1000+600);
        RedisUtil.setex("QRID_"+qrid,time, qrscene.toString(), 5);


        return jsonObject;
    }

    public  JSONObject getQrByAID(String AID) throws Exception {

        String rule = null;
        rule = RedisUtil.getKeyByDbidx("R_"+AID, 1);
        if(StringUtils.isBlank(rule)){
            if(QrConstant.PARAM.BI_AID_M.equals(AID)){
                rule = QrConstant.PARAM.QR_RULE_M;
            }else if(QrConstant.PARAM.BI_AID_R.equals(AID)){
                rule = QrConstant.PARAM.QR_RULE_R;
            }
        }

        JSONObject jsonObject = JSON.parseObject(rule);

        return jsonObject;
    }
    /**
     * json key相同值覆盖(忽略大小写)
     * @param json1
     * @param json2
     * @return json1
     * @throws JSONException
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
            //订购频道,默认设置五分钟
            String key = "V_"+cardId+"_"+serviceID;
            RedisUtil.setex(key, 300, tvTime, 3);

        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aftertime", Long.parseLong(tvTime));
        jsonObject.put("qrid", qrId);
        jsonObject.put("code", 3);
        return push(cardId, jsonObject.toString());

	}
	@Override
	public Result pushQrCode(String userId, Long ruleid,String startTime) throws Exception {
		String key2 = "R_"+ruleid;
        String rule = RedisUtil.getKeyByDbidx(key2,1);
		if(StringUtils.isBlank(rule)){
			throw new Exception("rule不能为空");
		}
        JSONObject jsonObject = JSON.parseObject(rule);
		long time = System.currentTimeMillis()+200000;
        if(StringUtils.isNotBlank(startTime)){
            time = new SimpleDateFormat("yyyyMMddHHmmss").parse(startTime).getTime();
        }
        jsonObject.put("startTime", time);

		jsonObject.put("cardid", userId);
		jsonObject.put("serviceid", jsonObject.getString("serviceid"));
		jsonObject.put("code", 1);
        jsonObject.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        Long qrid = RedisUtil.incr("QRPUT_QRID",2);
        jsonObject.put("qrid", qrid);

        jsonObject.put("qrurl", jsonObject.getString("qrurl")+"?qrid="+qrid);

        RedisUtil.setKey("QRID_"+qrid,
                userId+"~"+jsonObject.getString("serviceid")+"~"+ruleid+"~"+jsonObject.getLongValue("tvtime"), 5);
        String oldQrid = RedisUtil.getSet("Q_"+userId, String.valueOf(qrid), 5);
        RedisUtil.delKey(oldQrid, 5);

		return push(userId, jsonObject.toString());
	}
	
	
	public Result push(String userId, String message) throws Exception {
		if(StringUtils.isBlank(userId)){
			throw new Exception("userId不能为空");
		}

        RetResult retResult = pushManager.send(userId,message);
        retResult.setSedMsg(message);
        Logs.QRPUSH.info(retResult.toString());

        Result ret = new Result();
        if(null != retResult && 1 == retResult.getRetCode()){
            ret.setCode(0);
            ret.setMsg(retResult.retMsg);
            ret.setData(retResult.retData);
        }else if(null != retResult){
            ret.setCode(1);
            ret.setMsg(retResult.retMsg);
            ret.setData(retResult.retData);
        }
		return ret;
	}

}
