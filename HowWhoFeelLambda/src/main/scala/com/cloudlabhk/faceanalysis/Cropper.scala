package com.cloudlabhk.faceanalysis

import java.awt.RenderingHints
import java.awt.image.BufferedImage

// Content-aware image cropping with ChunkyPNG
// https://gist.github.com/a54cd41137b678935c91

final class Cropper(image: BufferedImage) {

  def cropAndScale(newWidth: Int = 100, newHeight: Int = 100) = {
    val size = math.min(image.getHeight, image.getWidth)
    resize(crop(size, size), newWidth, newHeight)
  }

  def crop(cropWidth: Int = 100, cropHeight: Int = 100) = {
    var (x, y, width, height) = (0, 0, image.getWidth, image.getHeight)
    val sliceLength = 16

    while ((width - x) > cropWidth) {
      val sliceWidth = math.min(width - x - cropWidth, sliceLength)

      val left = image.getSubimage(x, 0, sliceWidth, image.getHeight)
      val right = image.getSubimage(width - sliceWidth, 0, sliceWidth, image.getHeight)

      if (entropy(left) < entropy(right))
        x += sliceWidth
      else
        width -= sliceWidth
    }

    while ((height - y) > cropHeight) {
      val sliceHeight = math.min(height - y - cropHeight, sliceLength)

      val top = image.getSubimage(0, y, image.getWidth, sliceHeight)
      val bottom = image.getSubimage(0, height - sliceHeight, image.getWidth, sliceHeight)

      if (entropy(top) < entropy(bottom))
        y += sliceHeight
      else
        height -= sliceHeight
    }

    image.getSubimage(x, y, cropWidth, cropHeight)
  }

  private def histogram(image: BufferedImage) = {
    val hist = new Array[Int](256)
    for (d <- grayscaleData(image)) hist(d) = hist(d) + 1
    hist
  }

  /** http://www.mathworks.com/help/toolbox/images/ref/entropy.html */
  private def entropy(image: BufferedImage) = {
    val hist = histogram(grayscale(image))
    val area = (image.getWidth * image.getHeight).toDouble

    -hist.view.filter(_ > 0).foldLeft(0.0) { (e, freq) =>
      val p = freq / area
      e + p * math.log(p) // log2
    }
  }

  private def resize(img: BufferedImage, w: Int, h: Int): BufferedImage = {
    val resized = new BufferedImage(w, h, img.getType)
    val g = resized.createGraphics()

    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    g.drawImage(img, 0, 0, w, h, null)
    g.dispose()

    resized
  }

  private def grayscale(img: BufferedImage) = {
    val gray = new BufferedImage(img.getWidth, img.getHeight, BufferedImage.TYPE_BYTE_GRAY)
    val g = gray.getGraphics()
    g.drawImage(img, 0, 0, null)
    g.dispose()
    gray
  }

  private def grayscaleData(img: BufferedImage) =
    img.getData.getSamples(0, 0, img.getWidth, img.getHeight, 0, new Array[Int](img.getWidth * img.getHeight))
}
