package example

import breeze.plot.{Figure, plot}
import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph
import example.graph.Graph.{loadFromFile, numEdges, numVertices}
import example.graph.MST.{naiveKruskal, unionFindKruskal}
import example.util.Util.time

import scala.collection.mutable.ArrayBuffer

object UnionFindKruskal extends LazyLogging {

  def main(args: Array[String]): Unit = {
    val gs = loadFromFile()
    val buffer    = ArrayBuffer[(Double, Int)]()

    gs.foreach(graph => {
      val size = numVertices(graph)
      logger.info(s"executing for: ${size}")
      val result = time(unionFindKruskal(graph))
      buffer.append((result._1, size))
    })

    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt.xlabel = "size"
    plt.ylabel = "time ms"
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("kruskal-uf-plot.pdf")
  }

}
