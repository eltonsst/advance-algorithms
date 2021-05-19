package example

import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph
import example.graph.Graph.{numEdges, numVertices}

object Main extends App with LazyLogging {
  val graphs = Graph.loadFromFile()

  graphs.foreach(g => logger.info(
    s"""
      |${g._1}
      |vertices: ${numVertices(g._2)}
      |edges: ${numEdges(g._2)}
      |""".stripMargin)
  )
}
