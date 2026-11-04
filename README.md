# Store Application
The Store application keeps track of customers and orders in a database.

# Assumptions
This README assumes you're using a posix environment. It's possible to run this on Windows as well:
* Instead of `./gradlew` use `gradlew.bat`
* The syntax for creating the Docker container is different. You could also install PostgreSQL on bare metal if you prefer


# Prerequisites
This service assumes the presence of a postgresql 16.2 database server running on localhost:5433 (note the non-standard port)
It assumes a username and password `admin:admin` can be used.
It assumes there's already a database called `store`

You can start the PostgreSQL instance like this:
```shell
docker run -d \
  --name postgres \
  --restart always \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -v postgres:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:16.2 \
  postgres -c wal_level=logical
```

# Running the application
You should be able to run the service using
```shell
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/store SPRING_DATASOURCE_USERNAME=admin SPRING_DATASOURCE_PASSWORD=admin ./gradlew bootRun
```

The application uses Liquibase to migrate the schema. Some sample data is provided. You can create more data by reading the documentation in utils/README.md

# Data model
An order has an ID, a description, and is associated with the customer which made the order.
A customer has an ID, a name, and 0 or more orders.

# API
Two endpoints are provided:
   * /order
   * /customer

Each of them supports a POST and a GET. The data model is circular - a customer owns a number of orders, and that order necessarily refers back to the customer which owns it.
To avoid loops in the serializer, when writing out a Customer or an Order, they're mapped to CustomerDTO and OrderDTO which contain truncated versions of the dependent object - CustomerOrderDTO and OrderCustomerDTO respectively.

The API is documented in the OpenAPI file OpenAPI.yaml. Note that this spec includes part of one of the tasks below (the new /products endpoint)

# Tasks

1. Extend the order endpoint to find a specific order, by ID
2. Extend the customer endpoint to find customers based on a query string to match a substring of one of the words in their name
3. Users have complained that in production the GET endpoints can get very slow. The database is unfortunately not co-located with the application server, and there's high latency between the two. Identify if there are any optimisations that can improve performance
4. Add a new endpoint /products to model products which appear in an order:
      * A single order contains 1 or more products. 
      * A product has an ID and a description. 
      * Add a POST endpoint to create a product
      * Add a GET endpoint to return all products, and a specific product by ID
        * In both cases, also return a list of the order IDs which contain those products
      * Change the orders endpoint to return a list of products contained in the order

# Bonus points
1. Implement a CI pipeline on the platform of your choice to build the project and deliver it as a Dockerized image

# Notes on the tasks
Assume that the project represents a production application.
Think carefully about the impact on performance when implementing your changes
The specifications of the tasks have been left deliberately vague. You will be required to exercise judgement about what to deliver - in a real world environment, you would clarify these points in refinement, but since this is a project to be completed without interaction, feel free to make assumptions - but be prepared to defend them when asked.
There's no CI pipeline associated with this project, but in reality there would be. Consider the things that you would expect that pipeline to verify before allowing your code to be promoted
Feel free to refactor the codebase if necessary. Bad choices were deliberately made when creating this project.

# Docker image and CI

Build the application image locally with:

```shell
docker build -t store-api:local .
```

The GitHub Actions workflow runs `./gradlew check` for pushes and pull requests. It then builds the Docker image. Pull requests and non-default branches do not publish an image. Successful pushes to the default branch and version tags publish the image to GitHub Container Registry as `ghcr.io/<owner>/<repository>`.

The published image expects a PostgreSQL database. Supply `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` when running it. GitHub Actions uses the repository-provided `GITHUB_TOKEN` to publish; no personal registry token is stored in the repository.

## Run the published Docker image

You need Docker installed. The application also needs PostgreSQL; the API container does not include a database. The commands below start both containers on a private Docker network. The database data is kept in a named Docker volume.

If the GitHub Container Registry package is private, sign in first. `docker login` will prompt for your GitHub username and a personal access token with the `read:packages` permission. If the package is public, skip this step.

```shell
docker login ghcr.io
```

Pull the image published from the default branch:

```shell
docker pull ghcr.io/venkat1612/store-api-assessment:latest
```

Create a network and a persistent database volume:

```shell
docker network create store-net
docker volume create store-postgres-data
```

Start PostgreSQL. The port mapping makes the database available at `localhost:5433` from your computer; the API container will connect to it using the Docker network name `store-db` and port `5432`.

```shell
docker run -d \
  --name store-db \
  --network store-net \
  -p 5433:5432 \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -v store-postgres-data:/var/lib/postgresql/data \
  postgres:16.2 \
  postgres -c wal_level=logical
```

Wait until PostgreSQL reports that it is accepting connections:

```shell
docker exec store-db pg_isready -U admin -d store
```

Then start the API. Liquibase applies the database migrations during application startup.

```shell
docker run -d \
  --name store-api \
  --network store-net \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://store-db:5432/store \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=admin \
  ghcr.io/venkat1612/store-api-assessment:latest
```

Check that the APIs respond:

```shell
curl -i http://localhost:8080/customer
curl -i http://localhost:8080/order
curl -i http://localhost:8080/products
```

The `admin` credentials are for local assessment use only. Configure secure credentials for any deployed environment.

## GET performance choices

The GET repository methods declare entity graphs for the relationships needed by their response DTOs. This loads customer orders, order customers and products, and product order references as part of the repository read, avoiding the repeated lazy-load queries that can make high-latency database calls slow. Open Session in View is disabled so response mapping cannot silently issue additional database queries.

Customer substring search uses a PostgreSQL trigram GIN index on the uppercase customer name. It improves case-insensitive contains searches, at the cost of extra index storage and write work. Very short search terms may not provide enough trigrams for the index to help.

The assessment endpoints still return complete lists to preserve their specified response shape. If production data grows beyond a reasonable response size, add pagination with page metadata as a separate API contract change.
