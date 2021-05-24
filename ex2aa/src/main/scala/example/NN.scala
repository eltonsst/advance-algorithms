package example

import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.{numEdges, numVertices}
import example.graph.{Graph, TSP}
import example.util.Util

object NN extends App with LazyLogging {
  val graphs = Graph.loadFromFile()

  graphs.foreach(g => logger.info(
    s"""
      |${g._1}
      |vertices: ${numVertices(g._2)}
      |edges: ${numEdges(g._2)}
      |""".stripMargin)
  )

  graphs.foreach(g => {
    val tsp = Util.time(TSP.nearestNeighbor(g._2))
    logger.info(
      s"""
        |NN
        |tsp w for ${g._1} is: ${tsp._2.edges.map(_.w).sum} in time ${tsp._1} s
        |""".stripMargin)
  })
}
