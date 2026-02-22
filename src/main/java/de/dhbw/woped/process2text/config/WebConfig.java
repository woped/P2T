package de.dhbw.woped.process2text.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for the P2T Spring MVC application.
 *
 * <p>Handles CORS configuration using the {@code cors.allowed-origins} property,
 * which can be overridden via environment variable {@code CORS_ALLOWED_ORIGINS}.
 *
 * @author WoPeD Team
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
@Configuration
public class WebConfig {

  private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

  @Value("${cors.allowed-origins}")
  private String[] allowedOrigins;

  @Bean
  @Primary
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        logger.info("Applying CORS configuration for origins: {}", String.join(", ", allowedOrigins));
        registry
            .addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
