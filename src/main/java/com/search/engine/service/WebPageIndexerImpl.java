package com.search.engine.service;

import com.search.engine.domain.WebPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebPageIndexerImpl implements WebPageIndexer{

  private final WebPageService wpService;

  @Override
  @Async
  public void indexWebPage() {
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
            .map(WebPage::new).forEach(wpService::save);

  }

  private boolean isValidLink(String link) {
    return link != null && !link.trim().isEmpty() && !link.startsWith("file:") &&
            !link.startsWith("javascript:") && !link.startsWith("mailto:") &&
            !link.equals("void(0)");
  }
}
