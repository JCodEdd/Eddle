package com.search.engine.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = IndexProps.class)
@TestPropertySource("classpath:application.yaml")
class IndexPropsTest {

  @Autowired
  private  IndexProps indexProps;

  @Test
  void testProperties(){

  }

}