package com.cloudlabhk.faceanalysis

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.handlers.TracingHandler


abstract class RekognitionTools {

  import java.io.File

  import com.amazonaws.AmazonClientException
  import com.amazonaws.auth.{AWSCredentials, DefaultAWSCredentialsProviderChain}
  import com.amazonaws.regions.Regions
  import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
  import com.amazonaws.services.rekognition.model.Image

  val tmpFolder = "/tmp/"
  //val tmpFolder = "E:\\Working\\ImageTest"

  private def getCredentials(): Option[AWSCredentials] = {
    try {
      Some(new DefaultAWSCredentialsProviderChain().getCredentials)
    } catch {
      case e: AmazonClientException => None
    }
  }

  val credentials = getCredentials

  if (credentials.isEmpty)
    throw new Exception("Cannot get AWS Credential!")

  protected val amazonRekognition = AmazonRekognitionClientBuilder
    .standard
    .withRegion(Regions.US_WEST_2)
    .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder))
    .withCredentials(new DefaultAWSCredentialsProviderChain)
    .build


  protected def getImage(jpgFile: File): Option[Image] = {
    import java.io.FileInputStream
    import java.nio.ByteBuffer

    import com.amazonaws.util.IOUtils

    val inputStream = new FileInputStream(jpgFile)
    try {
      val imageBytes = Some(ByteBuffer.wrap(IOUtils.toByteArray(inputStream)))
      if (!imageBytes.isEmpty)
        return Some((new Image).withBytes(imageBytes.get))
    }
    finally if (inputStream != null) inputStream.close()
    None
  }
}
