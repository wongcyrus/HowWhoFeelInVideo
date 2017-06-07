package com.cloudlabhk

/**
  * Created by developer on 5/9/2017.
  */
object FaceProcessAnalysis {

  def main(args: Array[String]): Unit = {
    import com.cloudlabhk.faceanalysis.FaceCollectionTools

    val faceCollection = new FaceCollectionTools

    val collectionId = "student"
    val studentImageFolder = "E:\\Working\\ImageTest\\ICT"
    val classImagePath = "E:\\Working\\ImageTest\\2017_04_28_IMG_1827 - Copy.jpg"


    faceCollection.deleteCollection(collectionId)
    println(faceCollection.createCollection(collectionId))
    val indexResults = faceCollection.indexDirectory(collectionId, studentImageFolder)
    indexResults.foreach(println)

/*    val attendanceChecker = new AudienceChecker(collectionId)

    def getAttendanceTime: Date = {
      import java.nio.file.{Files, Paths}
      val path = Paths.get(classImagePath)
      new Date(Files.getLastModifiedTime(path).toMillis)
    }

    val d = getAttendanceTime
    println(d)

    val matchResults = attendanceChecker.matchFaceAnalysis(classImagePath)
    attendanceChecker.generateEmojiOverlap(classImagePath, "overlap", matchResults)

    matchResults.foreach(println)*/
  }
}
