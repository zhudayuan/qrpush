package com.maywidehb.qrpush.service;


import com.maywidehb.qrpush.entity.Result;

import java.util.List;

public interface QrManager {

    Result pushQrCode(String userId, Long ruleid, String startTime) throws Exception;

    Result pushReturn(String cardId, String serviceID, String tvTime, String qrId) throws Exception;

    Result pushCodeToClient(String cardid,String code,String qrid,String serviceid) throws Exception;

    Result pushQrList(List<String> jsonList) throws Exception;
}
