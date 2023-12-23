package com.search.engine.repository;

import com.search.engine.configuration.IndexProps;
import com.search.engine.domain.WebPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@EnableConfigurationProperties(value = IndexProps.class)
class WebPageRepositoryTest {

  @Autowired
  private WebPageRepository webPageRepository;
  WebPage webPage1;
  WebPage webPage2;
  WebPage webPage3;
  WebPage webPage4;

  @BeforeEach
  void setUp() {
    webPage1 = new WebPage(1L,null, "www.emptypage.com",null, null);
    webPage2 = new WebPage(2L, "Test wp 2", "www.testwp2.com", "unique2 wp", "A webPage for testing purposes");
    webPage3 = new WebPage(3L, "Test wp 3", "www.testwp3.com", "test3", "A unique3 webPage for testing purposes");
    webPage4 = new WebPage(4L, "Test unique4 wp 4", "www.testwp4.com", "test4", "A webPage for testing purposes");

    webPageRepository.saveAll(List.of(webPage2,webPage1,webPage3,webPage4));
  }

  @AfterEach
  void tearDown() {
    // NO need for this since each method runs on a transaction that's rolled back after execution
  }

  @Test
  void testExistsWebPageByUrl_FOUND() {
    assertThat(webPageRepository.existsWebPageByUrl("www.testwp2.com")).isTrue();
  }

  @Test
  void testExistsWebPageByUrl_NOT_FOUND() {
    assertThat(webPageRepository.existsWebPageByUrl("www.testwp5.com")).isFalse();
  }


  @Test
  void testFindByTitleIsNullAndDescriptionIsNull() {
    List<WebPage> expected = webPageRepository
            .findByTitleIsNullAndDescriptionIsNull(PageRequest.ofSize(1));
    assertThat(expected).containsOnly(webPage1);
  }

  //TESTS for findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase()

  //"unique2" found only in webPage2 keywords
  @Test
  void testSearchTerm_FOUND_OnKeywords_Only() {
    List<WebPage> expected = webPageRepository
            .findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase("unique2","unique2","unique2");
    assertThat(expected).containsOnly(webPage2);
  }

  //"unique3" found only in webPage3 description
  @Test
  void testSearchTerm_FOUND_OnDescription_Only() {
    List<WebPage> expected = webPageRepository
            .findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase("unique3","unique3","unique3");
    assertThat(expected).containsOnly(webPage3);
  }

  //"unique4" found only in webPage4 description
  @Test
  void testSearchTerm_FOUND_OnTitle_Only() {
    List<WebPage> expected = webPageRepository
            .findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase("unique4","unique4","unique4");
    assertThat(expected).containsOnly(webPage4);
  }
}