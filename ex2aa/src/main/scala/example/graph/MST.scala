package example.graph

import example.graph.Graph.{numVertices, sortedGraph}
import example.struct.UnionFind

import scala.annotation.tailrec

object MST {
  @tailrec
  private def recUnionFindKruskal(unionFindStruct: UnionFind, bufferMst: Graph, edges: Seq[Edge]): Graph =
    if (edges.isEmpty) bufferMst
    else {
      val e = edges.head
      if (unionFindStruct.find(e.u, e.v)) {
        recUnionFindKruskal(unionFindStruct, bufferMst, edges.tail)
      } else {
        recUnionFindKruskal(
          unionFindStruct.union(e.u, e.v),
          Graph(bufferMst.vertices, e +: bufferMst.edges),
          edges.tail
        )
      }
    }

  def unionFindKruskal(graph: Graph): Graph = {
    val graphAfterSort = sortedGraph(graph)
    val bufferMst = Graph(graph.vertices, Nil)
    val unionFindStruct = UnionFind.create(numVertices(graph) + 1)
    recUnionFindKruskal(unionFindStruct, bufferMst, graphAfterSort.edges)
  }
}