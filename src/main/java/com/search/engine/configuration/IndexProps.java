package com.search.engine.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "webpage.indexing")
@Data
public class IndexProps {
  
  private int urlstoindex;

  private String interval;
  
  private boolean interrupt;
  
  private String[] urls;

}
