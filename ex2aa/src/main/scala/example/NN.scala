package example

import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.{numEdges, numVertices}
import example.graph.{Graph, TSP}

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
    val tsp = TSP.nearestNeighbor(g._2)
    logger.info(
      s"""
        |NN
        |tsp w for ${g._1} is: ${tsp.edges.map(_.w).sum}
        |""".stripMargin)
  })
}
