.PHONY: build

build:
	sbt clean assembly

up:
	docker network create geo-net
	docker-compose -f build/docker-compose.yml up -d

delete:
	curl -X GET  'http://192.168.99.100:8983/solr/admin/collections?action=DELETE&name=geo_collection'

create:
	curl -X GET 'http://192.168.99.100:8983/solr/admin/collections?action=CREATE&name=geo_collection&numShards=1'
	curl -X POST -H 'Content-type:application/json' -d '@build/geo_collection.json'  'http://192.168.99.100:8983/solr/geo_collection/schema'

down:
	docker-compose -f build/docker-compose.yml down
	docker-compose -f build/docker-compose-spark-submit.yml down
	docker network rm geo-net

run:
	docker-compose -f build/docker-compose-spark-submit.yml build
	docker-compose -f build/docker-compose-spark-submit.yml up

reload:
	curl -X GET 'http://192.168.99.100:8983/solr/admin/collections?action=RELOAD&name=geo_collection'
