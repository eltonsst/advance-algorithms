package example

import breeze.plot.{Figure, plot}
import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.{loadFromFile, numVertices}
import example.graph.MST.{naiveKruskal, unionFindKruskal}
import example.util.Util.time

import scala.collection.mutable.ArrayBuffer

object UnionFindKruskal extends LazyLogging {

  def runKruskal(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()
    val totalTime = time {
      loadFromFile().foreach { graph =>
        val mst = time(unionFindKruskal(graph))
        logger.info(s"num vertices of mst: ${numVertices(mst._2)}, time: ${mst._1} ms")
        buffer.append((mst._1, numVertices(graph)))
      }
    }

    logger.info(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("kruskal-uf-plot.pdf")
  }

  def main(args: Array[String]): Unit = runKruskal()

}
