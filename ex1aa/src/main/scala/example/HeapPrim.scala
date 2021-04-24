package example

import breeze.plot.{plot, Figure}
import example.graph.Graph._
import example.graph.MST._
import example.util.Util.time
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ArrayBuffer

object HeapPrim extends LazyLogging {
  def runPrim(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()
    val totalTime = time {
      loadFromFile().foreach { graph =>
        val mst = time(heapPrim(graph))
        logger.info(s"mst vertices: ${mst._2.vertices.size}, time: ${mst._1} ms")
        buffer.append((mst._1, mst._2.vertices.size))
      //logger.info(s"result mst is: ${mst._2.edges}")
      }
    }

    logger.info(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("heap-plot.pdf")
  }

  def main(args: Array[String]): Unit = runPrim()

}
