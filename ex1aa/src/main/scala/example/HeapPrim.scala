package example

import breeze.plot.{Figure, plot}
import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph._
import example.graph.MST._
import example.util.Util.time

import scala.collection.mutable.ArrayBuffer

object HeapPrim extends LazyLogging {
  def runPrim(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()

    val totalTime = time {
      loadFromFile().foreach { graph =>
        val size = numVertices(graph)
        logger.info(s"executing for: ${size}")
        val result = time(heapPrim(graph))
        buffer.append((result._1, size))
      }
    }

    logger.info(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt.xlabel = "size"
    plt.ylabel = "time ms"
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq, name = "heapPrim")
    heapFigure.saveas("heap-plot.pdf")
  }

  def main(args: Array[String]): Unit = runPrim()

}
