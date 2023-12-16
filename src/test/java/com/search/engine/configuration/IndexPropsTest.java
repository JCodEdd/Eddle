package com.search.engine.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = IndexProps.class)
class IndexPropsTest {

  @Autowired
  private  IndexProps indexProps;

  @Test
  void testPropertiesAreBound(){
    String[] expectedUrls = {"https://www.w3schools.com", "https://edition.cnn.com"};

    assertEquals(10, indexProps.getUrlstoindex());
    assertEquals("PT15S", indexProps.getInterval());
    assertTrue(indexProps.isInterrupt());
    assertEquals(expectedUrls[0], indexProps.getUrls()[0]);
    assertEquals(expectedUrls[1], indexProps.getUrls()[1]);
  }

}