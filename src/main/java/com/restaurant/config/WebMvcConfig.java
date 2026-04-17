package com.restaurant.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  private final String uploadDir;
  private final String webPathPrefix;

  public WebMvcConfig(@Value("${app.upload.dir}") String uploadDir,
                      @Value("${app.upload.web-path-prefix}") String webPathPrefix) {
    this.uploadDir = uploadDir;
    this.webPathPrefix = webPathPrefix;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
    String pattern = normalizeWebPath(webPathPrefix) + "**";
    registry.addResourceHandler(pattern)
        .addResourceLocations(dir.toUri().toString())
        .setCachePeriod(3600);
  }

  private String normalizeWebPath(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "/uploads/";
    }
    String p = prefix.trim();
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    if (!p.endsWith("/")) {
      p = p + "/";
    }
    return p;
  }
}

