package example.graph

import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.AdjacencyList

import scala.annotation.tailrec

/** Basic immutable undirected graph class
  *
  * @constructor Create a new [[Graph]]
  * @param numVertices Number of vertices
  * @param numEdges Number of edges
  * @param adjacencyList Edge adjacency lists
  */
case class Graph(vertices: Seq[Int], edges: Seq[Edge], adjacencyList: AdjacencyList = Map.empty)

object Graph extends LazyLogging {
  private type AdjacencyList = Map[Int, Seq[Edge]]

  def numVertices(graph: Graph): Int                           = graph.vertices.length
  def numEdges(graph: Graph): Int                              = graph.edges.length
  def getDegree(graph: Graph, v: Int): Int                     = graph.adjacencyList(v).length
  def getAdjacencyList(graph: Graph, v: Int): (Int, Seq[Edge]) = (v, graph.adjacencyList(v))

  def buildGraph(edgeList: Seq[Edge]): Graph = {
    val vertices       = edgeList.flatMap(e => Seq(e.v, e.u)).distinct
    val adjacencyList1 = collection.mutable.Map() ++= edgeList.groupBy(_.u)
    val adjacencyList2 = edgeList
      .map(e => Edge(u = e.v, v = e.u, w = e.w))
      .groupBy(_.u)

    adjacencyList2.foreach(a => {
      if(adjacencyList1.contains(a._1)) adjacencyList1(a._1) = adjacencyList1(a._1) ++ a._2
      else adjacencyList1 += a
    })


    Graph(vertices, edgeList, Map.from(adjacencyList1))
  }

  /** Load vertices and edges from mst dataset of [[https://github.com/beaunus/stanford-algs/]]
    * @return A new [[Graph]]
    */
  def loadFromFile(): Seq[Graph] = {
    val wd = os.pwd / "src" / "main" / "resources" / "mst_dataset"
    os.list(wd).map { file =>
      logger.info(s"loading $file")
      val lines = os.read.lines(file)
      val edges = lines
        .map(_.split(" "))
        .filter(_.length > 2)
        .map(_.map(_.toInt))
        .map(arr => Edge(arr(0), arr(1), arr(2)))
      Graph.buildGraph(edges)
    }
  }

  def sortedGraph(graph: Graph): Graph = Graph(graph.vertices, graph.edges.sortBy(_.w), graph.adjacencyList)

  def dfs(vertices: Map[Int,Int], adjacencyList: AdjacencyList, u: Int, visited: Map[Int, Int]): Map[Int, Int] = {
    if(visited.contains(u)) visited // O(K)
    else if(!vertices.contains(u)) visited // O(k)
    else adjacencyList(u).foldLeft(visited + (u -> u))((updatedVisited, e) => dfs(vertices, adjacencyList, e.v, updatedVisited))
  }

  def isCyclic(graph: Graph, edge: Edge): Boolean = {
    if(graph.edges.isEmpty) false
    else {
      val vertexMap = graph.vertices.map(v => (v, v)).toMap // O(n)
      val result = dfs(vertexMap, graph.adjacencyList, edge.u, Map.empty) // O(numEdges)
      result.contains(edge.v)
    }
  }

}
