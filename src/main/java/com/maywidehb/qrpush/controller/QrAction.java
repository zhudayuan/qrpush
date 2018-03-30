package com.maywidehb.qrpush.controller;

import com.maywidehb.qrpush.entity.Result;
import com.maywidehb.qrpush.utils.Des3;
import com.maywidehb.qrpush.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/que")
public class QrAction {

    @RequestMapping("/queByQrid")
    public Result queByQrid(HttpServletRequest request)throws Exception{
        String qrid = request.getParameter("qrid");
        if(StringUtils.isBlank(qrid)){
            throw new Exception("qrid不能为空");
        }
        qrid = Des3.hexdecrypt(qrid);
        String qr = RedisUtil.getKeyByDbidx("QRID_"+qrid.trim(),5);

        Result result = new Result();
        result.setCode(0);
        result.setMsg("success");
        result.setData(qr);
        return result;
    }
}
