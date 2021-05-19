package example.graph

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

case class Graph(vertices: Seq[Int], edges: Seq[Edge])

object Graph extends LazyLogging {
  def numVertices(graph: Graph): Int                           = graph.vertices.length
  def numEdges(graph: Graph): Int                              = graph.edges.length

  def buildGraph(edgeList: Seq[Edge]): Graph = {
    val vertices       = edgeList.flatMap(e => Seq(e.v, e.u)).distinct
    Graph(vertices, edgeList)
  }

  private def computeEdges2D(curr: Array[Double], rest: List[Array[Double]]): Seq[Edge] =
    rest.map(that =>  {
      val u = curr(0).toInt
      val v = that(0).toInt
      val w = Math.sqrt(Math.pow(that(1) - curr(1), 2) + Math.pow(that(2) - curr(2), 2)).toInt
      Edge(u = u, v = v,  w = w)
    })

  private def makeEdges2D(curr: Array[Double], rest: List[Array[Double]]) : Seq[Edge] =
    if(rest.isEmpty) Nil
    else {
      val edges = computeEdges2D(curr, rest)
      val newCurr :: newRest = rest
      edges ++ makeEdges2D(newCurr, newRest)
    }

  private def makeEdgesGEO(ls: List[String]) : Seq[Edge] = {
    ???
  }

  def loadFromFile(): Seq[(String, Graph)] = {
    val wd = os.pwd / "src" / "main" / "resources" / "tsp_dataset"
    os.list(wd).map { file =>
      logger.info(s"loading file: $file")

      val name :: _ :: _ :: _ :: kindOfEdge :: _ :: ls = os.read.lines(file).toList

      val edges = kindOfEdge.split(":")(1).trim match {
        case "EUC_2D" =>
          val curr :: rest = ls
            .map(_.split(" ").filter(x => x.toDoubleOption.isDefined))
            .filter(_.nonEmpty)
            .map(_.map(_.toDouble))

          makeEdges2D(curr, rest)
        case _ => makeEdgesGEO(ls)
      }

      (name, Graph.buildGraph(edges))
    }
  }

}
