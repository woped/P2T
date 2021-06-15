package de.dhbw.woped.process2text.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@Configuration
public class SpringFoxConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "WoPeD - Process2Text Webservice",
                "This webservices can be used to translate a process model into a human readable text.",
                "3.8.0",
                "Terms of service",
                new Contact("Prof. Dr. Thomas Freytag", "www.woped.dhbw-karlsruhe.de/", "thomas.freytag@dhbw-karlsruhe.de"),
                "License of API", "API license URL", Collections.emptyList());
    }
}
