package com.tyse.scrutiny.micro.notification.config;

import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.annotation.PostConstruct;

/**
 * Configuration to add text template support to Thymeleaf.
 */
@Configuration
public class ThymeleafTextConfig {

    private final SpringTemplateEngine templateEngine;

    public ThymeleafTextConfig(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @PostConstruct
    public void addTextTemplateResolver() {
        ClassLoaderTemplateResolver textResolver = new ClassLoaderTemplateResolver();
        textResolver.setPrefix("templates/");
        textResolver.setSuffix(".txt");
        textResolver.setTemplateMode(TemplateMode.TEXT);
        textResolver.setCharacterEncoding("UTF-8");
        textResolver.setOrder(0);
        textResolver.setCheckExistence(true);
        textResolver.setCacheable(false);

        templateEngine.addTemplateResolver(textResolver);
    }
}
