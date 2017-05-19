package com.cloudlabhk.faceanalysis


class FaceCollectionTools extends RekognitionTools {

  import java.io.File

  import com.amazonaws.services.rekognition.model._

  def createCollection(collectionId: String) = {
    val request = new CreateCollectionRequest().withCollectionId(collectionId)
    amazonRekognition.createCollection(request).getCollectionArn
  }

  def deleteCollection(collectionId: String) = {
    val request = new DeleteCollectionRequest().withCollectionId(collectionId)
    amazonRekognition.deleteCollection(request)
  }

  private def indexFace(collectionId: String, externalImageId: String, attributes: String, image: Option[Image]): Option[IndexFacesResult] = image match {
    case Some(i) => Some(amazonRekognition.indexFaces(
      new IndexFacesRequest()
        .withImage(i)
        .withCollectionId(collectionId)
        .withExternalImageId(externalImageId)
        .withDetectionAttributes(attributes)))
    case _ => None
  }

  private def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) d.listFiles.filter(_.isFile).toList else List[File]()
  }

  def indexDirectory(collectionId: String, dir: String) = {
    val studentPhotos = getListOfFiles(dir)

    def getId(file: File): String = file.getName.split('.')(0).replaceAll("[^A-Za-z0-9]", "")

    studentPhotos.map(jpgFile => indexFace(collectionId, getId(jpgFile), "ALL", getImage(jpgFile)))
  }

}
