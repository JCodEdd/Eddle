package com.search.engine.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.search.engine.domain.WebPage;
import com.search.engine.service.IndexService;
import com.search.engine.service.WebPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = "application/json")
public class SearchController {
    
  private final WebPageService wpService;
  private final IndexService indexService;

  @GetMapping("search")
  public List<WebPage> search(@RequestParam("query") String query, @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> pageSize){
    return wpService.search(query, page, pageSize);
  }

  @GetMapping("indx")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void index(){
    indexService.start();
  }

  @GetMapping("stpindx")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void stopIndexing(){
    indexService.stop();
  }

  @PostMapping(path = "addurls", consumes = "application/json")
  @ResponseStatus(HttpStatus.CREATED)
  public List<WebPage> addUrlsToIndex(@RequestBody Map<String, List<String>> urlsmMap){
    try {
      return wpService.addUrls(urlsmMap.get("urls"));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

}