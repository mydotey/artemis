package org.mydotey.artemis.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author koqizhao
 *
 * Sep 21, 2018
 */
@ComponentScan("org.mydotey.artemis.server")
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class App extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        ArtemisServer.INSTANCE.init();
        return application.sources(App.class);
    }

    public static void main(String[] args) {
        ArtemisServer.INSTANCE.init();
        SpringApplication.run(App.class, args);
    }

}
