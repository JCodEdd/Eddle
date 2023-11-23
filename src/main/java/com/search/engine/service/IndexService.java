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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.search.engine.domain.WebPage;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "indexing")
public class IndexService{

  private final WebPageService wpService;

  private final TaskScheduler taskScheduler;

  @Setter
  private String interval;

  private ScheduledFuture<?> taskState;

  public void start(){
    log.info("Calling the indexing process");
    taskState = taskScheduler.scheduleAtFixedRate(new ScheduledTaskExecutor(), Duration.parse(interval));
  }

  public void stop(){
    log.info("Stopping the indexing process");
    if (taskState != null) {
    taskState.cancel(false);      
    } else return;
  }

  class ScheduledTaskExecutor implements Runnable{
    @Override
    public void run() {
      indexWebPage();
    }
  }

  @Async
  private void indexWebPage() {
    log.info("Starting indexing");
    List<WebPage> linksToIndex = wpService.getLinksToIndex();

    linksToIndex.forEach(wpToIndex -> {
      try {
        Document doc = Jsoup.connect(wpToIndex.getUrl()).get();
        parseAndSaveLinks(doc);

        String title = doc.title();
        String description = doc.select("meta[name=description]")
              .attr("content");
        String keywords = doc.select("meta[name=keywords]")
              .attr("content");

        wpToIndex.setTitle(title);
        wpToIndex.setDescription(description);
        wpToIndex.setKeywords(keywords);
        wpService.save(wpToIndex);
      } catch (HttpStatusException | MalformedURLException e) {
        wpService.delete(wpToIndex.getId());
        log.error("URL malformed or not accessible. Deleting URL from database: {}", wpToIndex.getUrl(), e);
      } catch (SocketTimeoutException e) {
        log.error("Connection timeout has occurred with: {}", wpToIndex.getUrl(), e);
      } catch (IOException e) {
        wpService.delete(wpToIndex.getId());
        log.error("Error processing URL. Deleting URL from database: {}", wpToIndex.getUrl(), e);
      } catch (Exception e) {
        wpService.delete(wpToIndex.getId());
        log.error("Unexpected Error. Deleting URL from database: {}", wpToIndex.getUrl(), e);
      }
    });
  }

  private void parseAndSaveLinks(Document doc) {
    Elements links = doc.select("a");
    if (links.isEmpty()) {return;}

    links.stream().map(link -> link.attr("abs:href"))
          .filter(this::isValidLink).filter(link -> !wpService.exist(link))
          .map(link -> new WebPage(link)).forEach(webPage -> wpService.save(webPage));

  }

  private boolean isValidLink(String link) {
    return link != null && !link.trim().isEmpty() && !link.startsWith("file:") &&
          !link.startsWith("javascript:") && !link.startsWith("mailto:") && 
          !link.equals("void(0)");
  }

}
