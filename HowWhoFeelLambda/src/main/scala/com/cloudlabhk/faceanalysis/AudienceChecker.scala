package com.cloudlabhk.faceanalysis

import java.awt.Color


class AudienceChecker(collectionId: String, faceMatchThreshold: Float) extends RekognitionTools {

  import java.awt.Rectangle
  import java.awt.image.BufferedImage
  import java.io.File
  import java.util
  import javax.imageio.ImageIO

  import com.amazonaws.services.rekognition.model._

  import scala.collection.mutable

  val maxFaces = 1

  private def searchFacesByImage(filePath: Option[String]): Option[SearchFacesByImageResult] = filePath match {
    case Some(f) => {
      val searchFacesByImageRequest = new SearchFacesByImageRequest()
        .withCollectionId(collectionId)
        .withImage(getImage(new File(f)).get)
        .withFaceMatchThreshold(faceMatchThreshold)
        .withMaxFaces(maxFaces)
      try
        Some(amazonRekognition.searchFacesByImage(searchFacesByImageRequest))
      catch {
        case e: InvalidParameterException => {
          println("searchFacesByImage " + f)
          println(e)
          None
        }
      }
    }
    case None => None
  }

  private def detectFaces(filePath: String): util.List[FaceDetail] = {
    val classImage = getImage(new File(filePath)).get
    val request = new DetectFacesRequest()
      .withImage(classImage)
      .withAttributes(Attribute.ALL)
    amazonRekognition.detectFaces(request).getFaceDetails
  }


  def matchFaceAnalysis(classImagePath: String) = {
    import collection.JavaConverters._

    val faceAnalysisResults = detectFaces(classImagePath).asScala

    import javax.imageio.ImageIO
    val classImage: BufferedImage = ImageIO.read(new File(classImagePath))

    def getBoundRectangle(boundingBox: BoundingBox) = {
      val left = boundingBox.getLeft * classImage.getWidth
      val top = boundingBox.getTop * classImage.getHeight
      val width = boundingBox.getWidth * classImage.getWidth
      val height = boundingBox.getHeight * classImage.getHeight
      new Rectangle(left.toInt, top.toInt, width.toInt, height.toInt)
    }

    val faceAndEmotion: mutable.Seq[(Rectangle, List[Emotion])] = faceAnalysisResults.map(f => (getBoundRectangle(f.getBoundingBox), f.getEmotions.asScala.toList))

    val faceImagesAndBoundBoxAndEmotion = faceAndEmotion.map(f => {
      println(f._1)
      if (f._1.x > 0 && f._1.y > 0
        && classImage.getHeight - (f._1.x + f._1.width) > 0
        && classImage.getWidth - (f._1.y + f._1.height) > 0) {
        val croppedImage = classImage.getSubimage(f._1.x, f._1.y, f._1.width, f._1.height)
        val filePathName = tmpFolder + File.separator + f._1 + ".png"
        javax.imageio.ImageIO.write(croppedImage, "png", new java.io.File(filePathName))
        (Some(filePathName), f._1, f._2)
      } else
        (None, f._1, f._2)
    })

    //Parallel the search request.
    val faceMatchAndBoundBoxAndEmotion = faceImagesAndBoundBoxAndEmotion.par.map(f => {
      searchFacesByImage(f._1) match {
        case Some(face) => {
          val id = face.getFaceMatches.asScala.headOption match {
            case Some(a) => a.getFace.getExternalImageId
            case None => "?????"
          }
          (id, f._2, f._3)
        }
        case None => ("????", f._2, f._3)
      }
    })
    faceMatchAndBoundBoxAndEmotion.seq
  }

  def generateEmojiOverlap(sourceImagePath: String, outputFileName: String,
                           faceMatchAndBoundBoxAndEmotion: mutable.Seq[(String, Rectangle, List[Emotion])]): String = {
    val canvas = ImageIO.read(new File(sourceImagePath))
    // get Graphics2D for the image
    val g = canvas.createGraphics()
    faceMatchAndBoundBoxAndEmotion.foreach(f => {
      import java.awt.{Font, RenderingHints}
      import javax.imageio.ImageIO

      val emojiPath = "/emoji/" + f._3.head.getType + ".png"
      val fileStream = this.getClass.getResourceAsStream(emojiPath)
      val emoji: BufferedImage = ImageIO.read(fileStream)
      g.drawImage(emoji, f._2.x, f._2.y, f._2.width, f._2.height, null)

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      val font: Font = new Font("Serif", Font.PLAIN, f._2.height / 4)
      g.setColor(Color.RED)
      g.setFont(font)
      g.drawString(f._1, f._2.x, f._2.y - (f._2.height / 4))

    })
    val emojiImageFilePathname = tmpFolder + File.separator + outputFileName + ".png"
    javax.imageio.ImageIO.write(canvas, "png", new java.io.File(emojiImageFilePathname))
    emojiImageFilePathname
  }
}
