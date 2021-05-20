package example.graph

import scala.annotation.tailrec

object TSP {

  @tailrec
  private def ricNearestNeighbor(graph: Graph, s: Int, tsp: Graph): Graph = {
    if(graph.edges.isEmpty) tsp
    else {
      val verticesInTsp = tsp.vertices.map(v => (v, v)).toMap
      val nearest = graph.edges
        // find nearest vertices not already inserted
         .filter(e =>
          (e.u == s && !verticesInTsp.contains(e.v))
            || (e.v == s) && !verticesInTsp.contains(e.u)
        )
        // take the smallest Vk+1 in term of cost
        .minByOption(_.w)
      if(nearest.isDefined) {
        val newS = if(s == nearest.get.v) nearest.get.u else nearest.get.v
        val newTsp = Graph(newS +: tsp.vertices, nearest.get +: tsp.edges)
        val newGraph = Graph(graph.vertices, graph.edges.filter(_ != nearest.get))
        ricNearestNeighbor(newGraph, newS, newTsp)
      } else {
        tsp
      }
    }
  }

  def nearestNeighbor(graph: Graph): Graph = ricNearestNeighbor(graph, 1, Graph(1 :: Nil, Nil))


}
