package com.search.engine.repository;

import com.search.engine.configuration.IndexProps;
import com.search.engine.domain.WebPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = IndexProps.class)
class WebPageRepositoryTest {

  @Autowired
  private WebPageRepository webPageRepository;
  WebPage webPage1;
  WebPage webPage2;
  WebPage webPage3;

  @BeforeEach
  void setUp() {
    webPage1 = new WebPage(1L, "Test wp 1", "www.testwp1.com", "test1", "A webPage for testing purposes");
    webPage2 = new WebPage(2L,null, "www.emptypage.com",null, null);
    webPage3 = new WebPage(3L, "Test wp 3", "www.testwp3.com", "test3", "A webPage for testing purposes");

    webPageRepository.saveAll(List.of(webPage1,webPage2,webPage3));
  }

  @AfterEach
  void tearDown() {
    webPage1 = null; webPage2 = null; webPage3 = null;
    webPageRepository.deleteAll();
  }

  @Test
  void testExistsWebPageByUrl() {
    assertThat(webPageRepository.existsWebPageByUrl("www.testwp1.com")).isTrue();
  }

  @Test
  void testFindByTitleIsNullAndDescriptionIsNull() {
    List<WebPage> expected = webPageRepository
            .findByTitleIsNullAndDescriptionIsNull(PageRequest.ofSize(1));
    assertThat(expected.get(0).getId()).isEqualTo(webPage2.getId());
    assertThat(expected.get(0).getUrl()).isEqualTo(webPage2.getUrl());
  }

  @Test
  void findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase() {
  }
}