package com.search.engine.service;

import com.search.engine.domain.WebPage;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class WebPageIndexerImplTest {

  @Mock
  private WebPageService webPageService;

  private WebPageIndexer webPageIndexer;
  private  AutoCloseable autoCloseable;

  @BeforeEach
  void setUp() {
    autoCloseable = MockitoAnnotations.openMocks(this);
    webPageIndexer = new WebPageIndexerImpl(webPageService);
  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
  }

  //Test that indexWebPage() properly indexes a web page: mocks Jsoup behaviour and ensures that the
  // WebPageService.save method is called with the correct WebPage object
  @Test
  void testIndexWebPage_SuccessfulIndexesWebPage() throws IOException {
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

      //mock Jsoup behaviour
      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenReturn(document);

      webPageIndexer.indexWebPage();

      //verify that the webPageService.save method is called with the updated WebPage object.
      verify(webPageService, times(1)).save(webPageToIndex);

      //verify save method is called with a WebPage whose url is the one parsed from indexed webPage
      verify(webPageService, times(1))
              .save(argThat(webPage -> webPage.getUrl().equals("http://parsedlink.com")));
    }
  }

  //Test that if the request URL is not an HTTP or HTTPS URL, or is otherwise malformed and a
  // MalformedURLException is thrown and proper handling is done
  @Test
  void testIndexWebPage_MalformedURLException_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      //mock Jsoup behaviour
      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(MalformedURLException.class);

      webPageIndexer.indexWebPage();

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an HttpStatusException when GETing the url content, proper handling is done
  @Test
  void testIndexWebPage_HttpStatusException_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(HttpStatusException.class);

      webPageIndexer.indexWebPage();

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an HttpStatusException when GETing the url content, proper handling is done
  @Test
  void testIndexWebPage_SocketTimeoutException_DoesntDeleteWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(SocketTimeoutException.class);

      webPageIndexer.indexWebPage();

      verify(webPageService, times(0)).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an error faced by Jsoup an IOException is thrown and proper handling is done
  @Test
  void testIndexWebPage_IOException_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      when(mockConnection.get()).thenThrow(IOException.class);

      webPageIndexer.indexWebPage();

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }

  //Test that in case of an error not handled somewhere else an IOException
  // is thrown and proper handling is done
  @Test
  void testIndexWebPage_Exception_DeletesWebPage() throws Exception {
    WebPage webPageToIndex = new WebPage("http://example.com");
    webPageToIndex.setId(1L);

    when(webPageService.getLinksToIndex()).thenReturn(List.of(webPageToIndex));
    when(webPageService.exist(anyString())).thenReturn(false);


    try (MockedStatic<Jsoup> jsoupMockedStatic = Mockito.mockStatic(Jsoup.class)) {

      Connection mockConnection = Mockito.mock(Connection.class);
      jsoupMockedStatic.when(() -> Jsoup.connect("http://example.com")).thenReturn(mockConnection);
      doAnswer(invocation -> { throw new Exception("Unexpected error"); }).when(mockConnection).get();

      webPageIndexer.indexWebPage();

      verify(webPageService).delete(webPageToIndex.getId());
    }

  }
}