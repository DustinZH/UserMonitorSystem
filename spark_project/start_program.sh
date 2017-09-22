#!/bin/bash

spark-submit \
--master spark://192.168.0.133:7077 \
--name stream_kafkanew \
--class SparkStreamingSingleNode \
--jars lib/spark-streaming-kafka-0-10_2.11-2.1.1.jar,lib/kafka-clients-0.10.2.1.jar \
Program-1.0-SNAPSHOT.jar hdfs://master:9000/program/checkpoint test3
