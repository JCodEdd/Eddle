package com.search.engine.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.search.engine.domain.WebPage;
import com.search.engine.repository.WebPageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebPageService {
    
  private final WebPageRepository repository;

  public List<WebPage> search(String query){
    String decodedQuery = URLDecoder.decode(query, StandardCharsets.UTF_8);
    List<String> qTerms = Arrays.asList(decodedQuery.split("\\s"));

    //All webpages that contain each search term
    List<List<WebPage>> allFromRepo = qTerms.stream().map(param -> searchInRepo(param))
                            .collect(Collectors.toList());
    
    //Taking only the webpages that contain all search terms 
    //This and the next step are a somewhat odd way to have the webpages that match all the terms at the top.
    Set<WebPage> allMatchesFirst = new LinkedHashSet<>(allFromRepo.get(0));
    for (List<WebPage> result : allFromRepo) {
        allMatchesFirst.retainAll(result);
    }

    //Adding the remaining webpages
    for(List<WebPage> result : allFromRepo){
      allMatchesFirst.addAll(result);
    }

    return new ArrayList<>(allMatchesFirst);
  }

  private List<WebPage> searchInRepo(String textSearch){
    return repository.findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase(textSearch, textSearch, textSearch);
  }

  public void delete(Long id){
    repository.deleteById(id);
  }

  public void save(WebPage webPage){
    repository.save(webPage);
  }

  public boolean exist(String link) {
    return repository.existsWebPageByUrl(link);
  }

  public List<WebPage> getLinksToIndex(){
    return repository.findTop20ByTitleIsNullAndDescriptionIsNull();
  }
}

