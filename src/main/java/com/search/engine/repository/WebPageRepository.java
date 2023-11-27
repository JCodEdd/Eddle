package com.search.engine.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.search.engine.domain.WebPage;


public interface WebPageRepository extends JpaRepository<WebPage, Long>{
  
  WebPage findByUrl(String url);
  
  boolean existsWebPageByUrl(String url);

  List<WebPage> findByTitleIsNullAndDescriptionIsNull(Pageable pageable);

  //Finds potential search matches on keywords, description, and title. 
  //Sometimes, neither the keywords nor the description correctly tag the specific content of a web page.
  List<WebPage> findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase(String keywords, String description, String title);

}
