package com.maywidehb.qrpush;

import com.maywidehb.qrpush.config.Logs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations={"classpath:application-bean.xml"})
public class QrpushApplication {

	public static void main(String[] args) {
		Logs.init();
		SpringApplication.run(QrpushApplication.class, args);
    }


}
