package com.search.engine.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "webpage")
@Data
public class WebPageProps {
  private int pageSize;
}
