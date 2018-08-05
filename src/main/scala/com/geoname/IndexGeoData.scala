package com.geoname

import org.apache.spark.SparkConf
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{SaveMode, SparkSession}

object IndexGeoData {

  def main(args: Array[String]): Unit = {

    // Running in simple standalone mode for POC and providing all the cores
    // Production mode master will be set to YARN or MESOS // Distributed resource manager scheduler
    val sparkConf =
      new SparkConf()
      //.setMaster("local[*]")
        .setAppName("index_geo_data")

    val sparkSession: SparkSession =
      SparkSession.builder().config(sparkConf).getOrCreate()

    // Define Schema to match the source data as described here at the bottom of the page
    val geonameSchema = StructType(
      Array(
        StructField("geonameid", IntegerType, false),
        StructField("name", StringType, false),
        StructField("asciiname", StringType, true),
        StructField("alternatenames", StringType, true),
        StructField("latitude", FloatType, true),
        StructField("longitude", FloatType, true),
        StructField("fclass", StringType, true),
        StructField("fcode", StringType, true),
        StructField("country", StringType, true),
        StructField("cc2", StringType, true),
        StructField("admin1", StringType, true),
        StructField("admin2", StringType, true),
        StructField("admin3", StringType, true),
        StructField("admin4", StringType, true),
        StructField("population", DoubleType, true),
        StructField("elevation", IntegerType, true),
        StructField("gtopo30", IntegerType, true),
        StructField("timezone", StringType, true),
        StructField("moddate", DateType, true)
      ))

    // Load the tab separated files
    // Single file or multiple files can be loaded
    // We can provide directory path instead of path to file name
    // It can also be loaded from HDFS / S3 etc..
    // The data can be loaded from local file system as in in this example.

    val geonamesDF = sparkSession.read
      .option("header", "false")
      .option("inferSchema", "false")
      .option("delimiter", "\t")
      .schema(geonameSchema)
      .csv(args(0))

    // 1. Log4j Logging --> Log stash --> elastic search --> Kibana/Grafana
    // 2. Amazon cloud watch agent

    //Apply Primer Transformation
    val primerDF = geonamesDF.select(
      col("geonameid").as("id"),
      col("name"),
      col("latitude"),
      col("longitude"),
      col("country").as("countryCode"),
      col("admin1").as("administrativeLevel1"),
      col("admin2").as("administrativeLevel2"),
      concat_ws(",", col("latitude"), col("longitude")).as("location")
    )

    // 1. Log4j Logging --> Log stash --> elastic search --> Kibana/Grafana
    // 2. Amazon cloud watch agent

    val options =
      Map("zkhost" -> "solr:9983", "collection" -> "geo_collection")

    //Index to Solr for Query and Search
    //primerDF.show()

    primerDF.write
      .format("solr")
      .options(options)
      .mode(SaveMode.Overwrite)
      .save()

    // 1. Log4j Logging --> Log stash --> elastic search --> Kibana/Grafana
    // 2. Amazon cloud watch agent

    //val df = sparkSession.read.format("solr").options(options).load
    //df.show()

    sparkSession.stop()

  }

}
