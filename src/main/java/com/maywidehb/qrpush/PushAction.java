package com.maywidehb.qrpush;

import com.maywidehb.qrpush.push.PushManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 *
 */
@RestController
@RequestMapping("/push")
public class PushAction {

    @Resource
    private PushManager pushManager;


    @RequestMapping()
    public String test() {
        return "/push";
    }

    @RequestMapping(value="/sendByBroadcast")
    public String sendByBroadcast(HttpServletRequest request) throws Exception{
        String tags = request.getParameter("tags");
        String condition = request.getParameter("condition");
        String message = request.getParameter("message");
        return pushManager.sendBroadcast(tags==null?null:Arrays.asList(tags), condition, message);
    }

    @RequestMapping(value="/sendByUserId")
    public String sendByUserId(HttpServletRequest request)  throws Exception{
        String userId = request.getParameter("userId");
        String message = request.getParameter("message");
        return pushManager.send(userId,message);
    }

    @RequestMapping(value="/sendByUserIds")
    public String sendByUserIds(HttpServletRequest request)  throws Exception{
        String userIds = request.getParameter("userIds");
        String message = request.getParameter("message");

        return pushManager.send(userIds!=null?Arrays.asList(userIds.split(",")):null,message);
    }



}
