package com.search.engine.controller;

import com.search.engine.domain.WebPage;
import com.search.engine.service.IndexService;
import com.search.engine.service.WebPageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest
class SearchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private WebPageService webPageService;
  @MockBean
  private IndexService indexService;
  WebPage webPage1;
  WebPage webPage2;
  WebPage webPage3;
  WebPage webPage4;
  List<WebPage> webPageList;


  @BeforeEach
  void setUp() {
    webPage1 = new WebPage(1L,null, "www.emptypage.com",null, null);
    webPage2 = new WebPage(2L, "Test wp 2", "www.testwp2.com", "test2", "A webPage for testing purposes");
    webPage3 = new WebPage(3L, "Test wp 3", "www.testwp3.com", "test3", "A webPage for testing purposes");
    webPage4 = new WebPage(4L, "Test wp 4", "www.testwp4.com", "test4", "A webPage for testing purposes");

  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testSearchOnlyQuery() {
    webPageList.add(webPage2);
    webPageList.add(webPage3);
    webPageList.add(webPage4);
    //when(webPageService.search("test")).thenReturn();
  }

  @Test
  void index() {
  }

  @Test
  void stopIndexing() {
  }

  @Test
  void addUrlsToIndex() {
  }
}