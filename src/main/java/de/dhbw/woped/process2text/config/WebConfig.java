package de.dhbw.woped.process2text.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for the P2T Spring MVC application.
 *
 * <p>This configuration class handles web-related settings, primarily
 * Cross-Origin Resource Sharing (CORS) configuration to allow the
 * P2T REST API to be accessed from frontend applications running on
 * different origins.
 *
 * <h2>CORS Configuration</h2>
 * <p>The following origins are allowed by default:
 * <ul>
 *   <li>{@code http://localhost:4200} - Angular development server</li>
 *   <li>{@code http://localhost:3000} - React/Node development server</li>
 * </ul>
 *
 * <h2>Allowed HTTP Methods</h2>
 * <ul>
 *   <li>GET - Read operations</li>
 *   <li>POST - Text generation requests</li>
 *   <li>PUT - Update operations</li>
 *   <li>DELETE - Delete operations</li>
 *   <li>OPTIONS - CORS preflight requests</li>
 * </ul>
 *
 * <p>Note: In production, you may want to configure allowed origins
 * via environment variables for security.
 *
 * @author WoPeD Team
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
@Configuration
public class WebConfig {

  private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

  /**
   * Creates a WebMvcConfigurer bean that configures CORS mappings.
   *
   * <p>This bean is marked as {@code @Primary} to ensure it takes precedence
   * over any other WebMvcConfigurer beans that might be auto-configured.
   *
   * @return a WebMvcConfigurer with CORS settings applied
   */
  @Bean
  @Primary
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      /**
       * Configures CORS mappings for all API endpoints.
       *
       * @param registry the CORS registry to configure
       */
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        logger.info("Applying CORS configuration");
        registry
            .addMapping("/**")
            .allowedOrigins("http://localhost:4200", "http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
