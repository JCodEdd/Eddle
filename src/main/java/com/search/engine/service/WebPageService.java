package com.search.engine.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.search.engine.configuration.IndexProps;
import com.search.engine.configuration.WebPageProps;
import com.search.engine.domain.WebPage;
import com.search.engine.repository.WebPageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebPageService {
    
  private final WebPageRepository repository;
  
  private final IndexProps indProps;

  private final WebPageProps webPageProps;

  public List<WebPage> search(String query, Optional<Integer> pageNumber, Optional<Integer> pageSize){
    String decodedQuery = URLDecoder.decode(query, StandardCharsets.UTF_8);
    List<String> qTerms = Arrays.asList(decodedQuery.split("\\s"));

    //All webpages that contain each search term
    List<List<WebPage>> allFromRepo = qTerms.stream().map(this::searchInRepo)
                            .toList();

    //Taking only the webpages that contain all search terms 
    //This and the next step are a somewhat bittersweet way to have the webpages that match all the terms at the top.
    Set<WebPage> allMatchesFirst = new LinkedHashSet<>(allFromRepo.get(0));
    for (List<WebPage> result : allFromRepo) {
        allMatchesFirst.retainAll(result);
    }

    //Adding the remaining webpages
    for(List<WebPage> result : allFromRepo){
      allMatchesFirst.addAll(result);
    }
   
    //Setting the ordered and clean results in a page holder so we can return the requested page
    PagedListHolder<WebPage> page = new PagedListHolder<>(new ArrayList<>(allMatchesFirst));
    
    //If not specified  the page size use the default from properties
    page.setPageSize(pageSize.orElse(webPageProps.getPageSize()));

    //If not specified  the page number return the first
    page.setPage(pageNumber.orElse(0));

    return page.getPageList();
  }

  private List<WebPage> searchInRepo(String textSearch){
    return repository.findByKeywordsContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrTitleContainingIgnoreCase(textSearch, textSearch, textSearch);
  }
  
  public List<WebPage> addUrls(List<String> urls) throws IllegalArgumentException {

    //Filters Urls that are already in the database
    List<String> onlyValidAndNewUrls = urls.stream().filter(this::isValidLink)
                .filter(url -> !repository.existsWebPageByUrl(url))
                .toList();
    if (onlyValidAndNewUrls.isEmpty()) {
      throw new IllegalArgumentException("Sent URLs were invalid or already in database");
    }
    
    List<WebPage> toReplace = repository
        .findByTitleIsNullAndDescriptionIsNull(PageRequest.of(0, onlyValidAndNewUrls.size()));

    IntStream.range(0, onlyValidAndNewUrls.size()).forEach(i -> toReplace.get(i).setUrl(onlyValidAndNewUrls.get(i)));
    repository.flush();
    return toReplace;
  }

  private boolean isValidLink(String link) {
    return link != null && !link.trim().isEmpty() && !link.startsWith("file:") &&
            !link.startsWith("javascript:") && !link.startsWith("mailto:") &&
            !link.equals("void(0)");
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
    return repository.findByTitleIsNullAndDescriptionIsNull(PageRequest.of(0, indProps.getUrlstoindex()));
  }

}

