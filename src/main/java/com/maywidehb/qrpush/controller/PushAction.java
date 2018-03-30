package com.maywidehb.qrpush.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

        cardid = Des3.hexdecrypt(cardid);
        return manager.pushReturn(cardid,serviceid,tvtime,qrid);
    }

    @RequestMapping("/pushCodeToClient")
    public Result pushCodeToClient(HttpServletRequest request)throws Exception{
//        code 1 推送要展示的二维码 2 参数(qrid) 取消等待展示的二维码 3 参数(qrid) 取消正在显示的二维码, 告知下次请求时间
//         4  客户端告诉服务端某个码可以展示  5 告诉服务端某个二维码停止展示
//        6  参数(serviceid)清除该卡号某个频道所有的二维码, 包括在展示和未展示的
//        7  清除该卡号所有的二维码, 包括在展示和未展示的
        String cardid = request.getParameter("cardid");
        String code = request.getParameter("code");//json字符串
        String qrid = request.getParameter("qrid");//json字符串
        String serviceid = request.getParameter("serviceid");//json字符串

        cardid = Des3.hexdecrypt(cardid);
        return manager.pushCodeToClient(cardid,code,qrid,serviceid);
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
        List<String> jsonList = new ArrayList<String>();

        Iterator<String> iterator = ((List<String>) reqMap.get("QRLIST")).iterator();
        while(iterator.hasNext()){
            String qr = JSON.toJSONString(iterator.next());
            jsonList.add(qr);
        }
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
            throw  new Exception("message不能为空");
        }
//        Des3.hexencrypt(message);

        return pushManager.sendBroadcast(tags==null?null:Arrays.asList(tags), condition, message);
    }

    @RequestMapping(value="/sendByUserId")
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

    @RequestMapping(value="/testPushQrcode")
    public Result testPushQrcode(HttpServletRequest request)throws Exception{

        String is = request.getParameter("userId_i");
        String js = request.getParameter("userId_j");
        List<String> jsonList = new ArrayList<String>();

        if(StringUtils.isNotBlank(is)&&StringUtils.isNotBlank(js)
                &&Integer.parseInt(is)>0&&Integer.parseInt(js)>0){
            String ret="{\"code\" : 0, \"data\" : {\"AFTERTIMES\" : 15000, \"AID\" : \"1\",\"BACKURL\" : \"http://*****/static/20180130121155553_568cjfx1.jpg\",\"COUNTDOWN\" : 1, \"DELIVERID\" : \"12\",\"QRHP\" : 300,\"QRSIZE\" : 400,\"QRURL\" : \" http://*****/static/20180130121155553_568?relation=12&tvtime1=1000&tvtime2=2000\",\"QRWP\": 300,\"WORKTIMES\":30000},\"msg\":\"success\"}";
            JSONObject json = JSON.parseObject(ret).getJSONObject("data");
            for(int i=Integer.parseInt(is);i>0;i--){
                for(int j=Integer.parseInt(js);j>0;j--){
                    json.put("LOGICDEVNO","userid-"+i+"_"+j);
                    json.put("CHANNELID","123,117");
                    json.put("QRTIME","20180321 16:49:25");
                    jsonList.add(json.toJSONString());
                }
            }
        }
        if(jsonList == null || jsonList.size()<1){
            throw new Exception("参数为空");
        }
        return manager.pushQrList(jsonList);
    }


}
