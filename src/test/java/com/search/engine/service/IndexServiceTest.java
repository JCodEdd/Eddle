package com.search.engine.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.search.engine.configuration.IndexProps;
import com.search.engine.configuration.SchedulerConfig;
import com.search.engine.configuration.WebPageProps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(SchedulerConfig.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = {WebPageProps.class, IndexProps.class})
class IndexServiceTest {

  @Mock
  private WebPageIndexer webPageIndexer;
  @Mock
  private TaskScheduler taskScheduler;
  @Mock
  private ScheduledFuture mockFuture;
  @Autowired
  private IndexProps indexProps;
  private IndexService indexService;
  private  AutoCloseable autoCloseable;

  @BeforeEach
  void setUp() {
    autoCloseable = MockitoAnnotations.openMocks(this);
    indexService = new IndexService(webPageIndexer, indexProps, taskScheduler);
  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
  }

  //Test that calls to start() correctly schedule the indexing task
  @Test
  void testStart() {
    when(taskScheduler.scheduleAtFixedRate( any(Runnable.class), any(Duration.class) )).thenReturn(mockFuture);

    indexService.start();

    verify(taskScheduler).scheduleAtFixedRate( any(IndexService.ScheduledTaskExecutor.class),
                                                eq(Duration.parse(indexProps.getInterval())) );
  }

  //Test that calls to stop() stop the indexing tasks canceling or not running ones according to props
  @Test
  void testStop() {
    when(taskScheduler.scheduleAtFixedRate( any(Runnable.class), any(Duration.class) )).thenReturn(mockFuture);

    indexService.start();
    indexService.stop();

    verify(mockFuture).cancel(indexProps.isInterrupt());
  }

  //Test that scheduled tasks run() call WebPageIndexer.indexWebPage()
  @Test
  void testScheduledTaskExecutor_Run_CallsIndexWebPage() {

    //capture scheduled task execution
    doAnswer(invocation -> {
      ((Runnable) invocation.getArgument(0)).run(); // Trigger the task's run method
      return null;
    }).when(taskScheduler).scheduleAtFixedRate(any(), any());

    indexService.start();

    verify(webPageIndexer, atLeast(1)).indexWebPage();
  }

  @Test
  void testStartLogsCorrectly() {
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(IndexService.class);
    logger.addAppender(listAppender);

    when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class))).thenReturn(mockFuture);

    indexService.start();

    List<ILoggingEvent> logsList = listAppender.list;
    // Verify the log messages, e.g., assert that "Calling the indexing process" is in the logsList
    assertThat(logsList).extracting("message").contains("Calling the indexing process");
  }

  @Test
  void testStopLogsCorrectly() {
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    Logger logger = (Logger) LoggerFactory.getLogger(IndexService.class);
    logger.addAppender(listAppender);

    when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class))).thenReturn(mockFuture);

    indexService.start();
    indexService.stop();

    List<ILoggingEvent> logsList = listAppender.list;
    // Verify the log messages, e.g., assert that "Calling the indexing process" is in the logsList
    assertThat(logsList).extracting("message").contains("Stopping the indexing process");
  }
}