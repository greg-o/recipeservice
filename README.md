# Spring Boot Scala Example

[![Build](https://github.com/jecklgamis/spring-boot-scala-example/actions/workflows/build.yml/badge.svg)](https://github.com/jecklgamis/spring-boot-scala-example/actions/workflows/build.yml)

This is an example Spring Boot app using Scala.

Docker run:
```
docker run -p 8080:8080 jecklgamis/spring-boot-scala-example:latest
```

What's In the Box?

* Java (18.0.2)
* Scala (3.0.2)
* Spring Boot (2.7.8)
* Maven build (12.3.1)
* Ubuntu Docker image
* Jetty web container
* HTTPS listener (prod profile) using self-signed certs
* JUnit5 tests
* Actuator endpoints (health, metrics)
* Build info, liveness and readiness probe endpoints
* Example Kubernetes deployment

## Building
Ensure you have Java 8, Docker, and Make installed.

Set up Postgres Database
    CREATE DATABASE recipe WITH ENCODING 'UTF8' LC_COLLATE='English_United States' LC_CTYPE='English_United States' OWNER rolename;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO userName;
    GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO userName;
    GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO userName;


```
make all
```
This will create build info, keystore, executable jar, and Docker image in one go. Explore the `Makefile` for details.

## Running

Run using Docker:
```bash
make run
```

Run using executable jar:
```bash
java -jar target/spring-boot-scala-example.jar
```

## Endpoints
Point your browser to the urls below or use `curl` in command line.
```bash
curl http://localhost:8080/
curl http://localhost:8080/buildInfo
curl http://localhost:8080/probe/live
curl http://localhost:8080/probe/ready
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/health
```
or you can run `./smoke-tests.sh`

Get list of recipes:
```bash
curl "http://localhost:8080/recipes/list?start=0&count=20"
```

Add recipe:
```bash
curl -X POST "http://localhost:8080/recipes/add" -d '{"name":"soup","description":"homemade","ingredients":[{"quantitySpecifier":"ounce","quantity":1.0,"ingredient":"water"}],"instructions":[{"instruction":"add water"}]}' -H "Content-Type: application/json"
```

Get recipe:
```bash
curl "http://localhost:8080/recipes/get/1"
```

Delete recipe:

```bash
curl -X DELETE "http://localhost:8080/recipes/delete/1"
```

Update recipe:

```bash
curl -X POST http://localhost:8080/recipes/update -d '{"recipeId":1,"name":"chili","description":"homemade","ingredients":[{"recipeId":1,"ingredientNumber":1,"quantitySpecifier":"Cup","quantity":1.0,"ingredient":"beer"}],"instructions":[{"recipeId":1,"instructionNumber":1,"instruction":"add beer"}]}' -H "Content-Type: application/json"
```

## Deploying To Kubernetes
Assumptions:
* You have `helm`command installed (Mac OS: `brew install helm`)
* You can deploy to a Kubernetes cluster that can access Docker Hub 

Build and install Helm chart:
```bash
cd deployment/k8s/helm
make install 
```
This creates:
* a service account
* a pod running on port 8080
* a service listening on port 80 (and implicitly endpoint resources corresponding to the number of pods)
* a deployment (and implicitly replicaset)
* an ingress for Istio (change `kubernetes.io/ingress.class` if you're using a different ingress controller such as nginx) 

To connect to the app locally, create a tunnel to the service:
```bash
kubectl port-forward service/spring-boot-scala-example 18080:80
curl http://localhost:18080
```

If you have ingress controller installed in your cluster, you can connect using
````
curl -v -H "Host:spring-boot-scala-example.local" http://<your-ingress-load-balancer-hostname>
````

## Contributing
Please raise issue or pull request! Thanks!