package com.maywidehb.qrpush.config;

import com.mpush.tools.config.CC;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public interface CF {
    Config cfg = load();

    static Config load() {

        Config config = ConfigFactory.load();
//        Config redis = ConfigFactory.parseFile(new File("resources/redis.conf"));
//        Config finalConfig = config.withFallback(redis);

        return config;
    }
    interface redis {

        Config redis = CC.cfg.getObject("spring.redis").toConfig();
        String host = redis.getString("host");
        int port = redis.getInt("port");
        String password = redis.getString("password");
        int maxTotal = redis.getInt("pool.max-total");
        int maxWait = redis.getInt("pool.max-wait");
        int maxIdle = redis.getInt("pool.max-idle");
        int minIdle = redis.getInt("pool.min-idle");
        int timeout = redis.getInt("timeout");

    }



}