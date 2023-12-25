package com.search.engine.service;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.search.engine.configuration.IndexProps;

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
