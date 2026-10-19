.PHONY: build up logs down

# Build both jars (the Dockerfiles copy them from target/).
build:
	mvn clean package

# Build fresh jars, rebuild the images and start everything in the background.
up: build
	docker compose up -d --build

# Follow only our own services, without the Kafka and ZooKeeper logs.
logs:
	docker compose logs -f producer consumer-1 consumer-2

# Stop everything and delete the Kafka data, so the next run starts with fresh topics.
down:
	docker compose down -v
