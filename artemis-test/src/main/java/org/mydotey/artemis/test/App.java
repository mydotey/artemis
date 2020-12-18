package org.mydotey.artemis.test;

import org.mydotey.artemis.server.ArtemisServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author koqizhao
 *
 * Sep 21, 2018
 */
@ComponentScan({ "org.mydotey.artemis.server", "org.mydotey.artemis.management" })
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class App {

    public static void main(String[] args) {
        System.setProperty("host.ip", "127.0.0.1");
        ArtemisServer.INSTANCE.init();
        SpringApplication.run(App.class, args);
    }

}
