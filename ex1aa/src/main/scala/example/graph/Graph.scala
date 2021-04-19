package example.graph

import scala.io.Source

/** Basic immutable undirected graph class
 *
 * @constructor Create a new [[Graph]]
 * @param numVertices Number of vertices
 * @param numEdges of edges
 * @param adjacencyList Edge adjacency lists
 */
case class Graph(numVertices: Int, numEdges: Int, adjacencyList: Map[Int, Seq[Edge]])

object Graph {
    def getDegree(graph: Graph, v: Int): Int = graph.adjacencyList(v).length
    def getAdjacencyList(graph: Graph, v: Int): Seq[Edge] = graph.adjacencyList(v)

    def buildGraph(edgeList: Seq[Edge]): Graph = {
        val numVertices = edgeList.map(e => e.u max e.v).max
        val adjacencyList = edgeList.groupBy(_.u)
        Graph(numVertices, edgeList.length, adjacencyList)
    }

    def loadFromFile(path: String): Graph = {
        val bufferedSource = Source.fromResource(path)
        val lines = bufferedSource.getLines().toSeq

        val edges = lines
          .map(_.split(" "))
          .filter(_.length > 2)
          .map(_.map(_.toInt))
          .map(arr => Edge(arr(0), arr(1), arr(2)))

        bufferedSource.close
        Graph.buildGraph(edges)
    }

}