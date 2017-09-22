import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Admin, Connection, ConnectionFactory, HTable}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.{KafkaUtils, LocationStrategies}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

/**
  * Created by dustin on 2017/7/12.
  */
object SparkStreaming {
  def main(args: Array[String]): Unit = {
    if(args.size != 1) {
      println("Please input args like:  kafkaTopic")
      System.exit(1)
    }
    def createStreamingContext():StreamingContext ={
      //设置spark配置信息，启动SC
      val sparkConf = new SparkConf()
      val ssc = new StreamingContext(sparkConf,Seconds(10)) //时间窗口为10秒，每10处理一批数据
      ssc.checkpoint("hdfs://keduox/program/chckpoint") // 自己设定，设置断点检查，存储元信息。断点后继续恢复

      //连接Kafka，配置Kafka
      val kafkaParams = Map[String,Object] (
        "bootstrap.servers" -> "master:9092,slave1:9092",
        "key.deserializer" -> classOf[StringDeserializer],
        "value.deserializer" -> classOf[StringDeserializer],
        "group.id" -> "Spark_Consumer",
        "auto.offset.reset" -> "latest",
        "enable.auto.commit"-> (false:java.lang.Boolean)
      )
      val topic =Array(args(0))  //设置kafka topic
      val lines = KafkaUtils.createDirectStream(ssc,LocationStrategies.PreferBrokers,Subscribe[String,String](topic,kafkaParams))
      lines.foreachRDD(rdd =>

      rdd.foreachPartition(KafkaPartition => {
        KafkaPartition.foreach(record => println(record.topic()+"\t"+record.key()+"\t"+record.value()))
      })
      )


      ssc
    }
    val ssc = StreamingContext.getOrCreate("hdfs://keduox/program/chckpoint",createStreamingContext _)
    ssc.start()
    ssc.awaitTermination()



  }
}


