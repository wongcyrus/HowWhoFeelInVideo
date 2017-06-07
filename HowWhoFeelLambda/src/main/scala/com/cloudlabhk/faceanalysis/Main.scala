package com.cloudlabhk.faceanalysis

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.handlers.TracingHandler


case class Event(bucket: String, key: String )

class Main {

  import java.awt.Rectangle
  import java.io.{File, InputStream}
  import java.net.URLDecoder
  import java.util.Date

  import com.amazonaws.services.lambda.runtime.Context
  import com.amazonaws.services.rekognition.model.Emotion
  import com.amazonaws.services.s3.AmazonS3ClientBuilder
  import com.amazonaws.services.s3.model.GetObjectRequest
  import com.fasterxml.jackson.databind.SerializationFeature
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  import scala.collection.mutable


  val collectionId = sys.env.get("collectionId")

  def handleRequest(input: InputStream, context: Context): Event = {
    val scalaMapper = {
      import com.fasterxml.jackson.databind.ObjectMapper
      new ObjectMapper().registerModule(new DefaultScalaModule)
    }
    val event = scalaMapper.readValue(input, classOf[Event])
    println(event)

    val sourceKey = event.key
    val sourceFileName = sourceKey.split('.')(0)
    val bucket = event.bucket
    val s3Client = AmazonS3ClientBuilder
      .standard
      .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder))
      .build()

    val classImagePath = "/tmp/" + sourceKey
    val imageFile = new File(classImagePath)
    s3Client.getObject(new GetObjectRequest(bucket, sourceKey), imageFile)

    val attendanceChecker = new AudienceChecker(collectionId.get)

    def getAttendanceTime: Date = {
      import java.nio.file.{Files, Paths}
      val path = Paths.get(classImagePath)
      new Date(Files.getLastModifiedTime(path).toMillis)
    }

    val d = getAttendanceTime
    println(d)

    val matchResults = attendanceChecker.matchFaceAnalysis(classImagePath)
    val emojiImage = attendanceChecker.generateEmojiOverlap(classImagePath, sourceFileName, matchResults)
    matchResults.foreach(println)


    s3Client.putObject(bucket, sourceFileName + ".json", getJson(sourceFileName, matchResults))
    s3Client.putObject(bucket, sourceFileName + ".png", new File(emojiImage))

    context.getLogger.log("OK")
    event
  }

  //HAPPY | SAD | ANGRY | CONFUSED | DISGUSTED | SURPRISED | CALM | UNKNOWN
  case class RowData(seq: String, id: String, happy: Float, sad: Float, angry: Float, confused: Float, disgusted: Float, surprised: Float, calm: Float, unknown: Float)

  def getJson(sourceFileName: String, obj: mutable.Seq[(String, Rectangle, List[Emotion])]) = {
    import org.json4s._
    import org.json4s.jackson.Serialization
    import org.json4s.jackson.Serialization.write
    org.json4s.jackson.JsonMethods.mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false)
    implicit val formats = Serialization.formats(NoTypeHints)
    val temp: mutable.Seq[(String, Map[String, Float])] = obj.map(a => (a._1, a._3.map(b => (b.getType -> b.getConfidence.toFloat)).toMap))

    val data = temp.map(a => RowData(sourceFileName, a._1,
      a._2.getOrElse("HAPPY", 0F),
      a._2.getOrElse("SAD", 0F),
      a._2.getOrElse("ANGRY", 0F),
      a._2.getOrElse("CONFUSED", 0F),
      a._2.getOrElse("DISGUSTED", 0F),
      a._2.getOrElse("SURPRISED", 0F),
      a._2.getOrElse("CALM", 0F),
      a._2.getOrElse("UNKNOWN", 0F)))
    write(data)
  }

  def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")
}
