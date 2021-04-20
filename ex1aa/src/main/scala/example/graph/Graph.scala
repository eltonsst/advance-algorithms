package example.graph

import scala.io.Source

/** Basic immutable undirected graph class
  *
  * @constructor Create a new [[Graph]]
  * @param numVertices Number of vertices
  * @param numEdges Number of edges
  * @param adjacencyList Edge adjacency lists
  */
case class Graph private (vertices: Seq[Int], edges: Seq[Edge], adjacencyList: Map[Int, Seq[Edge]])

object Graph {
  def numVertices(graph: Graph): Int = graph.vertices.length
  def numEdges(graph: Graph): Int = graph.edges.length
  def getDegree(graph: Graph, v: Int): Int              = graph.adjacencyList(v).length
  def getAdjacencyList(graph: Graph, v: Int): Seq[Edge] = graph.adjacencyList(v)

  def buildGraph(edgeList: Seq[Edge]): Graph = {
    val vertices   = edgeList.map(_.u).distinct
    val adjacencyList = edgeList.groupBy(_.u)
    Graph(vertices, edgeList, adjacencyList)
  }

  /** Load vertices and edges from mst dataset of [[https://github.com/beaunus/stanford-algs/]]
    * @param path path to dataset resource
    * @return A new [[Graph]]
    */
  def loadFromFile(path: String): Graph = {
    val bufferedSource = Source.fromResource(path)
    val lines          = bufferedSource.getLines().toSeq

    val edges = lines
      .map(_.split(" "))
      .filter(_.length > 2)
      .map(_.map(_.toInt))
      .map(arr => Edge(arr(0), arr(1), arr(2)))

    bufferedSource.close
    Graph.buildGraph(edges)
  }

}
