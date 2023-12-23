package com.search.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.engine.configuration.IndexProps;
import com.search.engine.configuration.SchedulerConfig;
import com.search.engine.configuration.WebPageProps;
import com.search.engine.domain.WebPage;
import com.search.engine.repository.WebPageRepository;
import com.search.engine.service.IndexService;
import com.search.engine.service.WebPageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@ContextConfiguration(classes = {SearchControllerTest.TestConfig.class, SearchController.class})
class SearchControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  private WebPageService webPageService;
  @MockBean
  private IndexService indexService;

  @MockBean
  private WebPageRepository webPageRepository;
  @MockBean
  private IndexProps indexProps;
  @MockBean
  private WebPageProps webPageProps;
  @MockBean
  private SchedulerConfig schedulerConfig;

  @Configuration
  static class TestConfig {
    @Bean
    public IndexProps indexProps() {
      IndexProps props = new IndexProps();
      props.setUrls(new String[]{"http://example.com"});
      return props;
    }
  }

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testSearch_WithOnlyQuery() throws Exception {

    given(webPageService.search("test", Optional.empty(),Optional.empty()))
            .willReturn(List.of(new WebPage(1L, "Test wp", "www.testwp.com", "test", "A webPage for testing purposes")));

    mockMvc.perform(get("/api/search")
            .param("query", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].url").value("www.testwp.com"));

  }

  @Test
  void testSearch_WithAllParameters() throws Exception {

    given(webPageService.search("test", Optional.of(1), Optional.of(5)))
            .willReturn(List.of(new WebPage(1L, "Test wp", "www.testwp.com", "test", "A webPage for testing purposes")));

    mockMvc.perform(get("/api/search")
                    .param("query", "test")
                    .param("page", "1")
                    .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].url").value("www.testwp.com"));

  }

  @Test
  void testSearch_WithWrongParameters_Returns400() throws Exception {

    mockMvc.perform(get("/api/search")
                    .param("query", "test")
                    .param("page", "1")
                    .param("size", "5L"))
            .andDo(print())
            .andExpect(status().isBadRequest());

  }

  @Test
  void index() throws Exception {
    mockMvc.perform(get("/api/indx"))
            .andExpect(status().isNoContent());

    verify(indexService, times(1)).start();
  }

  @Test
  void stopIndexing() throws Exception {
    mockMvc.perform(get("/api/stpindx"))
            .andExpect(status().isNoContent());

    verify(indexService, times(1)).stop();
  }

  @Test
  void addUrlsToIndex_AddsUrlsAndReturnsWebPages() throws Exception {
    List<String> urlsList = List.of("www.insertedwp1.com", "www.insertedwp2.com");

    given(webPageService.addUrls(urlsList))
            .willReturn(List.of(new WebPage(1L, null, "www.insertedwp1.com", null, null),
                                new WebPage(2L, null, "www.insertedwp2.com", null, null)));


    Map<String, List<String>> reqBody = new HashMap<>();
    reqBody.put("urls", urlsList);

    mockMvc.perform(post("/api/addurls").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reqBody)))
            .andExpect(status().isCreated())
            .andDo(print())
            .andExpect(jsonPath("$[0].url").value("www.insertedwp1.com"))
            .andExpect(jsonPath("$[1].url").value("www.insertedwp2.com"));
  }

  @Test
  void addUrlsToIndex_WhenCatchException_Returns400WithMessage() throws Exception {
    List<String> urlsList = List.of("www.alradyinsertedwp.com", "badUrl");

    given(webPageService.addUrls(urlsList))
            .willThrow(new IllegalArgumentException("Sent URLs were invalid or already in database"));


    Map<String, List<String>> reqBody = new HashMap<>();
    reqBody.put("urls", urlsList);

    mockMvc.perform(post("/api/addurls").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(reqBody)))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(status().reason(containsString("Sent URLs were invalid or already in database")));
  }

}