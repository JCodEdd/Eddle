package com.search.engine.service;

import com.search.engine.configuration.IndexProps;
import com.search.engine.configuration.WebPageProps;
import com.search.engine.domain.WebPage;
import com.search.engine.repository.WebPageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = {WebPageProps.class, IndexProps.class})
class WebPageServiceTest {

  @Mock
  private WebPageRepository webPageRepository;
  @Autowired
  private IndexProps indexProps;
  @Autowired
  private WebPageProps webPageProps;

  private WebPageService webPageService;
  private WebPage webPage1;
  private WebPage webPage2;
  private WebPage webPage3;
  private WebPage webPage4;
  private List<WebPage> webPageList;
  private  AutoCloseable autoCloseable;
  @BeforeEach
  void setUp() {
    autoCloseable = MockitoAnnotations.openMocks(this);
    webPageService = new WebPageService(webPageRepository,indexProps, webPageProps);

    webPage1 = new WebPage(1L,null, "www.emptypage.com",null, null);
    webPage2 = new WebPage(2L, "Test wp 2", "www.testwp2.com", "test2", "A webPage for testing purposes");
    webPage3 = new WebPage(3L, "Test wp 3", "www.testwp3.com", "test3", "A webPage 4 testing purposes");
    webPage4 = new WebPage(4L, "Webpage 4", "www.wp4.com", "Webpage4", "A webPage 4");

  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
    //TODO if webPageList is not used in any method delete declaration and reference here
    webPageList = null;
  }

  @Test
  void testSearch() {
    //encoding the query terms since thats how the method will receive them
    String query = URLEncoder.encode("test 4", StandardCharsets.UTF_8);

    when(webPageRepository.findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase("test","test","test"))
            .thenReturn(List.of(webPage2,webPage3));
    when(webPageRepository.findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase("4","4","4"))
            .thenReturn(List.of(webPage3,webPage4));

    //case 1: page # and size are not given, should return wp 3,2 & 4 in that order since only wp 3 contains
    //  all query terms, also "test" is the first term to be searched (present in 2) and "4" is the last one
    assertThat(webPageService.search(query, Optional.empty(), Optional.empty()))
            .containsSequence(List.of(webPage3,webPage2,webPage4));

    //verify the repository method was called (query parameters #) times
    verify(webPageRepository, times(2))
            .findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase(anyString(),anyString(),anyString());

    //case 2: page # 1 (starting from 0) and size 1 are given, should return
    //  wp 2 (the second page with all 3 wp in the same order ar the previous assertion)
    assertThat(webPageService.search(query, Optional.of(1), Optional.of(1)))
            .containsSequence(List.of(webPage2));
  }

  @Test
  void TestAddUrls_Throws_Ex_On() {
  }

  @Test
  void delete() {
  }

  @Test
  void save() {
  }

  @Test
  void exist() {
  }

  @Test
  void getLinksToIndex() {
  }
}