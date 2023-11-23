package com.search.engine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.search.engine.domain.WebPage;
import com.search.engine.repository.WebPageRepository;

@SpringBootApplication
public class EddleApplication {

	public static void main(String[] args) {
		SpringApplication.run(EddleApplication.class, args);
	}

    @Bean
    CommandLineRunner dataLoader(WebPageRepository webPageRepository){
    return args -> {
      WebPage w3 = new WebPage("https://www.w3schools.com");
      WebPage bbc = new WebPage("https://www.bbc.com");

      webPageRepository.save(w3);
      webPageRepository.save(bbc);
    };
  }

}
