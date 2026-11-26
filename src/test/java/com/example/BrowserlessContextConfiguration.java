package com.example;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrowserlessContextConfiguration {

    @Bean
    BrowserlessApplicationContext browserlessApplicationContext(
            ApplicationContext applicationContext) {
        return SpringBrowserlessApplicationContext.create(applicationContext,
                Application.class);
    }
}
