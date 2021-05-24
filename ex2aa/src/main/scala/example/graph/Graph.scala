package example.graph

import com.typesafe.scalalogging.LazyLogging

case class Graph(vertices: Seq[Int], edges: Seq[Edge])

object Graph extends LazyLogging {
  private type AdjacencyList = Map[Int, Seq[Edge]]

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
      val w = Math.sqrt(Math.pow(that(1) - curr(1), 2) + Math.pow(that(2) - curr(2), 2))
      Edge(u = u, v = v,  w = w.toInt)
    })

  private def makeEdges2D(curr: Array[Double], rest: List[Array[Double]]) : Seq[Edge] =
    if(rest.isEmpty) Nil
    else {
      val edges = computeEdges2D(curr, rest)
      val newCurr :: newRest = rest
      edges ++ makeEdges2D(newCurr, newRest)
    }

  def sortedGraph(graph: Graph): Graph = Graph(graph.vertices, graph.edges.sortBy(_.w))

  def computeEdgesGEO(curr: Array[Double], rest: List[Array[Double]]): Seq[Edge] = {
    rest.map(that => {
      val u = curr(0).toInt
      val v = that(0).toInt

      val pi = math.Pi
      val xLat = pi * (math.floor(curr(1)) + 5.0 * (math.floor(curr(1)) - math.floor(curr(1))) / 3.0) / 180.0
      val xLong = pi * (math.floor(curr(2)) + 5.0 * (math.floor(curr(2)) - math.floor(curr(2))) / 3.0) / 180.0
      val yLat = pi * (math.floor(that(1)) + 5.0 * (math.floor(that(1)) - math.floor(that(1))) / 3.0) / 180.0
      val yLong = pi * (math.floor(that(2)) + 5.0 * (math.floor(that(2)) - math.floor(that(2))) / 3.0) / 180.0
      val q1 = math.cos(xLong - yLong)
      val q2 = math.cos(xLat - yLat)
      val q3 = math.cos(xLat + yLat)
      val rrr = 6378.388

      val w = math.floor(rrr * math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0)

      Edge(u = u, v = v,  w = w)
    })
  }

  def makeEdgesGEO(curr: Array[Double], rest: List[Array[Double]]): Seq[Edge] = {
    if(rest.isEmpty) Nil
    else {
      val edges = computeEdgesGEO(curr, rest)
      val newCurr :: newRest = rest
      edges ++ makeEdgesGEO(newCurr, newRest)
    }
  }

  def loadFromFile(): Seq[(String, Graph)] = {
    val wd = os.pwd / "src" / "main" / "resources" / "tsp_dataset"
    os.list(wd).map { file =>
      logger.info(s"loading file: $file")

      val name :: _ :: _ :: _ :: kindOfEdge :: _ :: ls = os.read.lines(file).toList

      val edges = kindOfEdge.split(":")(1).trim match {
        case "EUC_2D" =>
          val first :: rest = ls
            .map(_.split(" ").filter(x => x.toDoubleOption.isDefined))
            .filter(_.nonEmpty)
            .map(_.map(_.toDouble))

          makeEdges2D(first, rest)
        case _ =>
          val first :: rest = ls
            .map(_.split(" ").filter(x => x.toDoubleOption.isDefined))
            .filter(_.nonEmpty)
            .map(_.map(_.toDouble))
          makeEdgesGEO(first, rest)
      }

      (name.toLowerCase, Graph.buildGraph(edges))
    }
  }

  def dfs(adjacencyList: AdjacencyList, u: Int, visited: Seq[Int]): Seq[Int] = {
    if(visited.contains(u)) visited
    else adjacencyList(u).foldLeft(u +: visited)((updatedVisited, e) => dfs(adjacencyList, e.v, updatedVisited))
  }

  def buildAdjList(edges: Seq[Edge]): AdjacencyList = {
    val adjacencyList1 = edges.groupBy(_.u).toSeq
    val adjacencyList2 = edges.map(e => Edge(u = e.v, v = e.u, w = e.w)).groupBy(_.u).toSeq
    val adjacencyList3 = adjacencyList1 ++ adjacencyList2
    val adjacencyList4 = adjacencyList3.flatMap(_._2).groupBy(_.u)
    adjacencyList4
  }

}
