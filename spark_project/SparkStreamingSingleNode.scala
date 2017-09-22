import java.util.Calendar

import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.{KafkaUtils, LocationStrategies}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

/**
  * Created by dustin on 2017/7/12.
  */
object SparkStreamingSingleNode {
  def main(args: Array[String]): Unit = {
    if(args.size != 2) {
      println("Please input args like: checkpoint kafkaTopic")
      System.exit(1)
    }
    def createStreamingContext():StreamingContext ={
      //设置spark配置信息，启动SC
      val sparkConf = new SparkConf()
      val ssc = new StreamingContext(sparkConf,Seconds(10)) //时间窗口为10秒，每10处理一批数据
      ssc.checkpoint(args(0)) // 自己设定，设置断点检查，存储元信息。断点后继续恢复

      //连接Kafka，配置Kafka
      val kafkaParams = Map[String,Object] (
        "bootstrap.servers" -> "master:9092",
        "key.deserializer" -> classOf[StringDeserializer],
        "value.deserializer" -> classOf[StringDeserializer],
        "group.id" -> "Spark_Consumer",
        "auto.offset.reset" -> "latest",
        "enable.auto.commit"-> (false:java.lang.Boolean)
      )
      val topic =Array(args(1))  //设置kafka topic
      val lines = KafkaUtils.createDirectStream(ssc,LocationStrategies.PreferBrokers,Subscribe[String,String](topic,kafkaParams))
      lines.foreachRDD(rdd =>

      rdd.foreachPartition(KafkaPartition => {

        val HBaseConf = HBaseConfiguration.create()
        HBaseConf.set("hbase.zookeeper.quorum","master")
        HBaseConf.set("hbase.zookeeper.property.clientPort","2181")

        val HBaseConn = ConnectionFactory.createConnection(HBaseConf)
        val tableRecord  = HBaseConn.getTable(TableName.valueOf("keduox:clickTabletest"))
        val tableResult  = HBaseConn.getTable(TableName.valueOf("keduox:TableResulttest"))

        KafkaPartition.foreach(record => {

          val array :Array[String]=record.value().split(",")
          val  rowkey = record.key()+array(0)+array(1)//日志类型+时间+设备号
          val get = new Get(Bytes.toBytes(rowkey))
          val result = tableRecord.get(get)  //从表中相关rowkey的各个信息

          if(result.getRow == null) {  //判断是否有重复点击
            val put = new Put(Bytes.toBytes(rowkey))
            put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("time"),Bytes.toBytes("NewTestData"))
            tableRecord.put(put)

            tableResult.incrementColumnValue(Bytes.toBytes(record.key()), Bytes.toBytes("cf"), Bytes.toBytes("Num"), 1L)  //点击总数加1
            tableResult.incrementColumnValue(Bytes.toBytes(record.key()), Bytes.toBytes("cf"), Bytes.toBytes("NoDuplicateNum"), 1L) //排重总数加1
            // 显示当前日志类型的点击总数和排重点击
            val getResult = new Get(Bytes.toBytes(record.key()))
            val result2 = tableResult.get(getResult)

            val  cells = result2.rawCells()
            for(cell <-cells) {
              println("This is result rowkey:"+Bytes.toString(CellUtil.cloneRow(cell))
              +"\t cf: "+Bytes.toString(CellUtil.cloneFamily(cell))
              +"\t cq: "+Bytes.toString(CellUtil.cloneQualifier(cell))
              +"\t value: "+Bytes.toLong(CellUtil.cloneValue(cell)))
            }

          }
          else {
              //有重复点击，只对不排重次数加1
              tableResult.incrementColumnValue(Bytes.toBytes(record.key()),Bytes.toBytes("cf"),Bytes.toBytes("Num"),1L) //点击总数加1

            // 显示当前日志类型的点击总数和排重点击
            val getResult = new Get(Bytes.toBytes(record.key()))
            val result2 = tableResult.get(getResult)

            val  cells = result2.rawCells()
            for(cell <-cells) {
              println("This is result rowkey:"+Bytes.toString(CellUtil.cloneRow(cell))
                +"\t cf: "+Bytes.toString(CellUtil.cloneFamily(cell))
                +"\t cq: "+Bytes.toString(CellUtil.cloneQualifier(cell))
                +"\t value: "+Bytes.toLong(CellUtil.cloneValue(cell)))
            }
            }
          //println(record.topic()+"\t"+record.key()+"\t"+record.value())


        })
        tableRecord.close()
        tableResult.close()
        HBaseConn.close()///不关掉 hbase分区连接会出现：并发连接数超过了其承载量。Connection reset by peer
      })
      )


      ssc
    }
    val ssc = StreamingContext.getOrCreate(args(0),createStreamingContext _)
    ssc.start()
    ssc.awaitTermination()



  }
}
/*  //hbase不支持序列化所以不能共享这个连接
object HBaseUtils extends Serializable{
  private val HBaseConf = HBaseConfiguration.create()
  HBaseConf.set("hbase.zookeeper.quorum","master")
  HBaseConf.set("hbase.zookeeper.property.clientPort","2181")
  private val conn = ConnectionFactory.createConnection(HBaseConf)
  def getHBaseConnection : Connection  = conn
}
*/
