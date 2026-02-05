package de.dhbw.woped.process2text.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for application metrics using Micrometer.
 *
 * <p>This configuration defines metrics beans that are used to track
 * HTTP request statistics and API performance. The metrics are automatically
 * exposed via Spring Boot Actuator endpoints and can be integrated with
 * monitoring systems like Prometheus, Grafana, or other time-series databases.
 *
 * <h2>Defined Metrics</h2>
 * <ul>
 *   <li><b>http_requests_total:</b> Counter for total HTTP requests received</li>
 *   <li><b>http_request_duration_seconds:</b> Timer for request processing duration</li>
 *   <li><b>api_call_duration_seconds:</b> Timer for API call processing time</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>Metrics are injected into controllers and services using {@code @Qualifier}:
 * <pre>{@code
 * @Autowired
 * @Qualifier("httpRequestsTotal")
 * private Counter httpRequestsTotal;
 * }</pre>
 *
 * @author WoPeD Team
 * @see io.micrometer.core.instrument.MeterRegistry
 */
@Configuration
public class MetricsConfig {

  /**
   * Creates a counter for tracking the total number of HTTP requests.
   *
   * <p>This counter is incremented each time an HTTP request is processed
   * by the P2T controller endpoints.
   *
   * @param registry the Micrometer meter registry
   * @return a Counter instance registered with the name "http_requests_total"
   */
  @Bean(name = "httpRequestsTotal")
  public Counter httpRequestsTotal(MeterRegistry registry) {
    return Counter.builder("http_requests_total")
        .description("Total number of HTTP requests")
        .register(registry);
  }

  /**
   * Creates a timer for measuring HTTP request processing duration.
   *
   * <p>This timer records the time taken to process each HTTP request,
   * useful for monitoring response latency and identifying slow endpoints.
   *
   * @param registry the Micrometer meter registry
   * @return a Timer instance registered with the name "http_request_duration_seconds"
   */
  @Bean(name = "httpRequestDuration")
  public Timer httpRequestDuration(MeterRegistry registry) {
    return Timer.builder("http_request_duration_seconds")
        .description("HTTP request duration in seconds")
        .register(registry);
  }

  /**
   * Creates a timer for measuring API call processing duration.
   *
   * <p>This timer is tagged with the application name for multi-service
   * environments and can be used to track external API call performance.
   *
   * @param registry the Micrometer meter registry
   * @return a Timer instance registered with the name "api_call_duration_seconds"
   */
  @Bean(name = "apiCallDuration")
  public Timer apiCallDuration(MeterRegistry registry) {
    return Timer.builder("api_call_duration_seconds")
        .description("API call processing duration")
        .tag("application", "p2t-service")
        .register(registry);
  }
}
