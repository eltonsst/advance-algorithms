package example

import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.{numEdges, numVertices}
import example.graph.{Graph, TSP}
import example.util.Util

object Approx extends App with LazyLogging {
  val graphs = Graph.loadFromFile()

  graphs.foreach(g => logger.info(
    s"""
      |${g._1}
      |vertices: ${numVertices(g._2)}
      |edges: ${numEdges(g._2)}
      |""".stripMargin)
  )

  graphs.foreach(g => if(g._2.edges.nonEmpty) {
    val tsp = Util.time(TSP.approx2(g._2))
    logger.info(
      s"""
         |2 APPROX
         |tsp cost for ${g._1} is: ${tsp._2} in time ${tsp._1} s
         |""".stripMargin)
  })
}
