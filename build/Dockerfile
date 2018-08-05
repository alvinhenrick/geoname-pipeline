FROM bde2020/spark-submit:2.3.1-hadoop2.7

RUN mkdir -p /app
COPY target/geoname-pipeline-assembly-0.1.jar /app/geoname-pipeline-assembly-0.1.jar

ENV ENABLE_INIT_DAEMON false
ENV SPARK_MASTER_NAME spark-master
ENV SPARK_MASTER_PORT 7077
ENV SPARK_APPLICATION_JAR_LOCATION /app/geoname-pipeline-assembly-0.1.jar
ENV SPARK_APPLICATION_MAIN_CLASS com.geoname.IndexGeoData
ENV SPARK_APPLICATION_ARGS "file:///app/data/cities1000.txt"