package com.maywidehb.qrpush.push;

import com.maywidehb.qrpush.push.service.MPushManager;
import com.maywidehb.qrpush.push.service.PushService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("/push")
public class PushAction {

    @Resource
    private PushService pushService;

    @Resource
    private MPushManager mPushManager;


    @RequestMapping()
    public String test() {
        System.out.println("11111111");
        return "/push";
    }

    @RequestMapping(value="/sendByBroadcast")
    public String sendByBroadcast(HttpServletRequest request) throws Exception{
        String tags = request.getParameter("tags");
        String condition = request.getParameter("condition");
        String message = request.getParameter("message");

        pushService.sendBroadcast(Arrays.asList(tags), condition, message, null);

        return "推送广播消息成功";
    }

    @RequestMapping(value="/sendByUserId")
    public String sendByUserId(HttpServletRequest request)  throws Exception{
        String userId = request.getParameter("userId");
        String message = request.getParameter("message");

        pushService.send(userId,message);
        return "推送广播消息成功";
    }


    @RequestMapping(value="/getOnlineUserNum")
    public long getOnlineUserNum(HttpServletRequest request) throws Exception{
        String ip = request.getParameter("ip");
        return mPushManager.getOnlineUserNum(ip);
    }

    @RequestMapping(value="/getOnlineUserList")
    public List<String> getOnlineUserList(HttpServletRequest request) throws Exception{
        String ip = request.getParameter("ip");
        return mPushManager.getOnlineUserList(ip);
    }
}
