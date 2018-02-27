/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.maywidehb.qrpush.push.service;


import com.mpush.api.push.PushCallback;

import java.util.List;

/**
 * Created by ohun on 16/9/15.
 *
 * @author ohun@live.cn (夜色)
 */
public interface PushService {
    boolean sendBroadcast(List<String> tags, String condition, String message, PushCallback callback) throws Exception;

    boolean send(String userId, String message) throws Exception;

    boolean send(String userId,List<String> userIds ,String message, PushCallback callback) throws Exception;

    boolean send(String userId ,String message, PushCallback callback) throws Exception;

}
