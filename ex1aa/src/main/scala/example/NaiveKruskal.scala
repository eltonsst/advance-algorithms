package example

import breeze.plot.{plot, Figure}
import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.{loadFromFile, numVertices}
import example.graph.MST.naiveKruskal
import example.util.Util.time

import scala.collection.mutable.ArrayBuffer

object NaiveKruskal extends LazyLogging {

  def runKruskal(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()
    val totalTime = time {
      loadFromFile().foreach { graph =>
        val numV = numVertices(graph)
        val mst = time(naiveKruskal(graph))
        logger.info(s"num vertices of mst: ${numV}, time: ${mst._1} ms")
        logger.info(s"mst weight ${mst._2.edges.map(_.w).sum}")
        buffer.append((mst._1, numV))
      }
    }

    logger.info(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("kruskal-naive-plot.pdf")
  }

  def main(args: Array[String]): Unit = runKruskal()

}
