package com.search.engine;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.search.engine.configuration.IndexProps;
import com.search.engine.domain.WebPage;
import com.search.engine.repository.WebPageRepository;

@SpringBootApplication
public class EddleApplication {

	public static void main(String[] args) {
		SpringApplication.run(EddleApplication.class, args);
	}

  @Bean
  CommandLineRunner dataLoader(WebPageRepository webPageRepository, IndexProps props){
  return args -> {
    Arrays.asList(props.getUrls()).stream().forEach(url -> webPageRepository.save(new WebPage(url)));
  };
  }

}
