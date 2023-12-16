package com.search.engine.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "webpage.indexing")
@Data
public class IndexProps {
  
  private int urlstoindex;

  //TODO not sure if should delete this due not been used
  private int resultsNumber;

  private String interval;
  
  private boolean interrupt;
  
  private String[] urls;

}
