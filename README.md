# <img src="https://github.com/JCodEdd/search-engine/blob/main/src/main/resources/static/img/fullLogo.png" width="90" /> Search engine 
This project implements a basic web crawler and search engine using Spring Boot with a lightweight frontend UI for interaction. 
Its main purpose is to showcase technical skills and architectural concepts.

## Technologies
* Spring Boot
* MySQL
* Jsoup (HTML processing)
* Logback
* Swagger UI & OpenAPI docs
* Basic HTML/CSS/JS frontend

## Architecture
The project follows a layered architecture with data stored in a MySQL database. Configuration is managed via YAML file, and logging 
is implemented to the console and rolling file appender.

## Key Features
* Scheduled and asynchronous crawling starting from seed URLs in a breadth-first approach
* Indexing of page content
* Search across indexed data
* Submit URLs to be processed first in next crawl batch
* Configurable properties
  * Crawl schedule interval (webpage.indexing.interval)
  * Initial seed URLs list (webpage.indexing.urls)
  * Interrupt or not the running crawls (webpage.indexing.interrupt)
  * Number of URLs to index per batch (webpage.indexing.urlstoindex)
  * Default search page size (webpage.pageSize)
* Basic frontend for interaction

<img src="https://github.com/JCodEdd/search-engine/blob/main/src/main/resources/static/img/eddle.png" width="400" /> <img src="https://github.com/JCodEdd/search-engine/blob/main/src/main/resources/static/img/results.png" width="400" />

## Getting Started
1. Clone the repository
2. Import the project to your favorite IDE
3. Set properties you like the most
4. Run the project
5. Index and search
### Access the UI at:
* http://localhost:8080 for frontend to search, start/stop indexing and set next URLs to crawl on top of pending URL
* http://localhost:8080/swagger-ui/index.html for OpenAPI API documentation
