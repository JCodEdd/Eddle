package com.search.engine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.search.engine.domain.WebPage;
import com.search.engine.service.IndexService;
import com.search.engine.service.WebPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {
    
  private final WebPageService wpService;
  private final IndexService indexService;

  @GetMapping("search")
  public List<WebPage> search(@RequestParam("query") String query){
    return wpService.search(query);
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
}