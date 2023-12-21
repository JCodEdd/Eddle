package com.search.engine.service;

import com.search.engine.configuration.IndexProps;
import com.search.engine.configuration.SchedulerConfig;
import com.search.engine.configuration.WebPageProps;
import com.search.engine.domain.WebPage;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(SchedulerConfig.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = {WebPageProps.class, IndexProps.class})
class IndexServiceTest {

  @Mock
  private WebPageService webPageService;
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
    indexService = new IndexService(webPageService, indexProps, taskScheduler);
  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
  }

  //Test that calls to start() correctly schedule the indexing task
  @Test
  public void testStart() {
    when(taskScheduler.scheduleAtFixedRate( any(Runnable.class), any(Duration.class) )).thenReturn(mockFuture);

    indexService.start();

    verify(taskScheduler).scheduleAtFixedRate( any(IndexService.ScheduledTaskExecutor.class),
                                                eq(Duration.parse(indexProps.getInterval())) );
  }

  //Test that calls to stop() stop the indexing tasks canceling or not running ones according to props
  @Test
  public void testStop() {
    when(taskScheduler.scheduleAtFixedRate( any(Runnable.class), any(Duration.class) )).thenReturn(mockFuture);

    indexService.start();
    indexService.stop();

    verify(mockFuture).cancel(eq(indexProps.isInterrupt()));
  }

  /**
   * Test that indexWebPage() properly indexes a web page: simulates the successful execution of the scheduled task,
   including the parsing and saving of web page information, and ensures that the webPageService.save method
   is called with the correct WebPage object
   */
  @Test
  public void testScheduledTask_SuccessfulIndexesWebPage() throws IOException {
    WebPage webPageToIndex = new WebPage("http://example.com");
    String title = "title";
    String description = "description";
    String keywords = "keywords";

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);

    //create an empty Jsoup Document to insert the info IndexService should parse
    Document document = Document.createShell("testDoc");
    document.title(title);

    //create and insert the description
    Element newElement = document.createElement("meta");
    newElement.attr("name", "description");
    newElement.attr("content", description);
    document.head().appendChild(newElement);

    //create and insert the keywords
    Element newElement1 = document.createElement("meta");
    newElement1.attr("name", "keywords");
    newElement1.attr("content", keywords);
    document.head().appendChild(newElement1);

    //create and insert the links
    Element newElement2 = document.createElement("a");
    newElement2.attr("abs:href", "http://parsedlink.com");
    document.body().appendChild(newElement2);

    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenReturn(document);

      //introduce a CountDownLatch to stop test thread until the "mocked indexing" finishes
      CountDownLatch latch = new CountDownLatch(1);

      //capture asynchronous executions using CountDownLatch
      doAnswer(invocation -> {
        ((Runnable) invocation.getArgument(0)).run(); // Trigger the task's run method
        latch.countDown(); // Countdown to signal completion
        return null;
      }).when(taskScheduler).scheduleAtFixedRate(any(), any());

      indexService.start();
      latch.await(5, TimeUnit.SECONDS); // Wait for the scheduled task to complete

      //verify that the webPageService.save method is called with the updated WebPage object.
      verify(webPageService, times(1)).save(eq(webPageToIndex));

      //verify save method is called with a WebPage whose url is the one parsed from indexed webPage
      verify(webPageService, times(1))
              .save(argThat(webPage -> webPage.getUrl().equals("http://parsedlink.com")));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  //Test that if the request URL is not an HTTP or HTTPS URL, or is otherwise malformed and a
  // MalformedURLException is thrown and proper handling is done
  @Test
  public void testIndexWebPage_MalformedURLException_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(MalformedURLException.class);

      //introduce a CountDownLatch to stop test thread until the "mocked indexing" finishes
      CountDownLatch latch = new CountDownLatch(1);

      //capture asynchronous executions using CountDownLatch
      doAnswer(invocation -> {
        ((Runnable) invocation.getArgument(0)).run(); // Trigger the task's run method
        latch.countDown(); // Countdown to signal completion
        return null;
      }).when(taskScheduler).scheduleAtFixedRate(any(), any());

      indexService.start();
      latch.await(5, TimeUnit.SECONDS); // Wait for the scheduled task to complete

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an HttpStatusException when GETing the url content, proper handling is done
  @Test
  public void testIndexWebPage_HttpStatusException_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(HttpStatusException.class);

      //introduce a CountDownLatch to stop test thread until the "mocked indexing" finishes
      CountDownLatch latch = new CountDownLatch(1);

      //capture asynchronous executions using CountDownLatch
      doAnswer(invocation -> {
        ((Runnable) invocation.getArgument(0)).run(); // Trigger the task's run method
        latch.countDown(); // Countdown to signal completion
        return null;
      }).when(taskScheduler).scheduleAtFixedRate(any(), any());

      indexService.start();
      latch.await(5, TimeUnit.SECONDS); // Wait for the scheduled task to complete

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an error faced by Jsoup an IOException is thrown and proper handling is done
  @Test
  public void testIndexWebPage_IOException_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(IOException.class);

      //introduce a CountDownLatch to stop test thread until the "mocked indexing" finishes
      CountDownLatch latch = new CountDownLatch(1);

      //capture asynchronous executions using CountDownLatch
      doAnswer(invocation -> {
        ((Runnable) invocation.getArgument(0)).run(); // Trigger the task's run method
        latch.countDown(); // Countdown to signal completion
        return null;
      }).when(taskScheduler).scheduleAtFixedRate(any(), any());

      indexService.start();
      latch.await(5, TimeUnit.SECONDS); // Wait for the scheduled task to complete

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an error not handled somewhere else an IOException
  // is thrown and proper handling is done
  @Test
  public void testIndexWebPage_Exception_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      doAnswer(invocation -> { throw new Exception("Unexpected error"); }).when(mockConnection).get();

      //introduce a CountDownLatch to stop test thread until the "mocked indexing" finishes
      CountDownLatch latch = new CountDownLatch(1);

      //capture asynchronous executions using CountDownLatch
      doAnswer(invocation -> {
        ((Runnable) invocation.getArgument(0)).run(); // Trigger the task's run method
        latch.countDown(); // Countdown to signal completion
        return null;
      }).when(taskScheduler).scheduleAtFixedRate(any(), any());

      indexService.start();
      latch.await(5, TimeUnit.SECONDS); // Wait for the scheduled task to complete

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

}