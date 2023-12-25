package com.search.engine.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.search.engine.configuration.IndexProps;
import com.search.engine.domain.WebPage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexService{


  private final WebPageIndexer webPageIndexer;
  private final IndexProps indProps;

  private final TaskScheduler taskScheduler;
  private ScheduledFuture<?> taskState;

  public void start(){
    log.info("Calling the indexing process");
    taskState = taskScheduler.scheduleAtFixedRate(new ScheduledTaskExecutor(), Duration.parse(indProps.getInterval()));
  }

  public void stop(){
    log.info("Stopping the indexing process");
    if (taskState != null) {
    taskState.cancel(indProps.isInterrupt());      
    }
  }

  class ScheduledTaskExecutor implements Runnable{
    @Override
    public void run() {
      webPageIndexer.indexWebPage();
    }
  }

}
