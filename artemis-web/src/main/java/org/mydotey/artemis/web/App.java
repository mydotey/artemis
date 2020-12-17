package org.mydotey.artemis.web;

import org.mydotey.artemis.cluster.ClusterManager;
import org.mydotey.artemis.management.ManagementInitializer;
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
@ComponentScan("org.mydotey.artemis.web")
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class App extends SpringBootServletInitializer {

    public App() {
        try {
            ManagementInitializer.INSTANCE.init();
            ClusterManager.INSTANCE.init();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
