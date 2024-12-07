# spring-boot / ip-filter-service

This is a sample for Recarga Pay microservice using spring boot.
Monitoring is done using [Prometheus](https://prometheus.io/) and [Grafana](https://grafana.com/).

## Description

The application is a simple service that  mangages users' wallets.
To have high availability, the application uses Redis as a cache with eviction policy.

The technology stack is as follows:
- Java 21
- Spring Boot
- JUnit 5
- Maven
- Redis

## Possible improvements
- Apply DB sharding by user country
- Indexing wallet balance
- Better error handling
- Implement a circuit breaker
- Implement a rate limiter

## How to run

You must install [docker](https://www.docker.com/), and use docker-compose command.
Run application as follows.

```bash
$ cd recarga-pay-ms
$ docker-compose up -d
``` 

Three applications are going to start.
(Spring boot web application takes a little time to start up.)

| Application | URL |
|-------------|------|
|spring boot web application | http://localhost:8080 |
|Prometheus | http://localhost:9090 |
|Grafana | http://localhost:3000 |

Prometheus monitors spring boot application.
Grafana visualizes prometheus data source.

## Set up Grafana's data source

You can login to Grafana by `admin/admin`.
You set up prometheus data source as follows.

|item| value |
|---|-----|
|Type|Prometheus|
|URL|http://localhost:9090|
|Access|direct|
|Scrap interval|5s|

![スクリーンショット 2018-02-26 12.21.27.png](https://qiita-image-store.s3.amazonaws.com/0/110216/3e577ff2-3d72-77e2-8667-ac25810794b9.png)

## Set up graph

I don't explain here.
Refer to [Prometheus documentation](https://prometheus.io/docs/introduction/overview/) and [Grafana documentation](http://docs.grafana.org/).


## Testing application's API with Postman

Download and install [Postman](https://www.postman.com/).
Refer to [import a collection](https://learning.postman.com/docs/getting-started/importing-and-exporting/importing-data/) into Postman.
Import into Postman the file `recarga-pay-ms.postman_collection.json` located in the root of the project.