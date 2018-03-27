package com.maywidehb.qrpush.controller;

import com.maywidehb.qrpush.entity.Result;
import com.maywidehb.qrpush.entity.RetResult;
import com.maywidehb.qrpush.service.QrManager;
import com.maywidehb.qrpush.service.push.PushManager;
import com.maywidehb.qrpush.utils.Des3;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.net.URLDecoder.decode;

/**
 *
 */
@RestController
@RequestMapping("/push")
public class PushAction {

    @Resource
    private PushManager pushManager;

    private Result result = new Result();

    @Resource
    private QrManager manager;
    /**
     */
    @RequestMapping("/pushReturn")
    @SuppressWarnings("unchecked")
    public Result pushReturn(@RequestBody Map<String,String> reqMap)throws Exception{
        String cardid =  reqMap.get("cardid");
        String serviceid = reqMap.get("serviceid");
        String tvtime = reqMap.get("tvtime");
        String qrid =  reqMap.get("qrid");

        //qrid= Des3.hexdecrypt(qrid)
        return manager.pushReturn(cardid,serviceid,tvtime,qrid);
    }

    @RequestMapping("/pushStop")
    public Result pushStop(HttpServletRequest request)throws Exception{
        String qrid = request.getParameter("qrid");
        String code = request.getParameter("code");

        //code= Des3.hexdecrypt(code)
        return manager.pushStop(qrid, Integer.valueOf(code));
    }
    @RequestMapping("/postQrCode")
    public Result postQrCode(HttpServletRequest request)throws Exception{
        String ruleId = request.getParameter("ruleid");
        String userId = request.getParameter("userid");
        String startTime = request.getParameter("startTime");

        if(StringUtils.isBlank(ruleId)
                || StringUtils.isBlank(userId)){
            throw new Exception("ruleid或者userid为空, 请检查");
        }
        return manager.pushQrCode(userId, Long.parseLong(ruleId.trim()), startTime);
    }

    @RequestMapping("/pushQrcode")
    @SuppressWarnings("unchecked")
    public Result pushQrList(@RequestBody Map<String,Object> reqMap)throws Exception{
        List<String> jsonList = (List<String>)reqMap.get("QRLIST");
        if(jsonList == null || jsonList.size()<1){
            throw new Exception("参数为空");
        }
        return manager.pushQrList(jsonList);
    }

    @RequestMapping()
    public String test() {
        return "/push";
    }

    @RequestMapping(value="/sendByBroadcast")
    public RetResult sendByBroadcast(HttpServletRequest request) throws Exception{

        String tags = request.getParameter("tags");
        String condition = request.getParameter("condition");
        String message = request.getParameter("message");
        if(StringUtils.isBlank(message)){
            throw  new Exception("");
        }
        Des3.hexencrypt(message);

        return pushManager.sendBroadcast(tags==null?null:Arrays.asList(tags), condition, message);
    }

    @RequestMapping(value="/sendByUserId" ,  produces = { "application/json;charset=UTF-8" })
    public RetResult sendByUserId(HttpServletRequest request)  throws Exception{
        String userId = request.getParameter("userId");
        String message = request.getParameter("message");

        message = decode(message, "UTF-8");

        return pushManager.send(userId,message);
    }

    @RequestMapping(value="/sendByUserIds")
    public RetResult sendByUserIds(HttpServletRequest request)  throws Exception{
        String userIds = request.getParameter("userIds");
        String message = request.getParameter("message");
        message = decode(message, "UTF-8");
        return pushManager.send(userIds!=null?Arrays.asList(userIds.split(",")):null,message);
    }



}
