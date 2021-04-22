package example.graph

import example.struct.{Entry, Heap, VectorHeap}
import Graph._

import scala.annotation.tailrec

/** Functions to create a minimum spanning tree for undirected weighted graphs.
  */
object MST {
  type MST = Map[Int, Int]

  @tailrec
  private def updateHeap(
      edgesAdjacentToU: (Int, Seq[Edge]),
      heap: Heap[Int, Int],
      entry: Entry[Int, Int],
      mst: MST
  ): (MST, Heap[Int, Int]) =
    if (edgesAdjacentToU._2.isEmpty) (mst, heap) // trivial
    else {
      // update the key value
      if (edgesAdjacentToU._2.head.w < entry.key) {
        val updatedMst = mst + (edgesAdjacentToU._1 -> edgesAdjacentToU._2.head.v)
        updateHeap(
          (edgesAdjacentToU._1, edgesAdjacentToU._2.tail),
          heap.decreaseKey(entry.value, edgesAdjacentToU._2.head.w),
          entry,
          updatedMst
        )
      } else
        updateHeap((edgesAdjacentToU._1, edgesAdjacentToU._2.tail), heap, entry, mst)
    }

  @tailrec
  private def recHeapPrim(heap: Heap[Int, Int], graph: Graph, mst: MST): MST =
    if (heap.isEmpty) mst
    else {
      // let u be the min extracted from the heap
      val (maybeU, heapAfterExtraction)     = heap.extractMin
      // get list of vertex v adjacent to u
      val edgesAdjacentToU                  = getAdjacencyList(graph, maybeU.get.value)
      // update state of the heap and current MST
      val (mstAfterUpdate, heapAfterUpdate) = updateHeap(edgesAdjacentToU, heapAfterExtraction, maybeU.get, mst)
      recHeapPrim(heapAfterUpdate, graph, mstAfterUpdate)
    }

  def heapPrim(graph: Graph, s: Int = 1): MST = {
    // set key of initial node to 0, infinite for the others
    val entries = graph.vertices.map(v => if (v == s) Entry(0, v) else Entry(Int.MaxValue, v))
    // heap is the priority queue, contains all nodes not in the tree
    val heap    = VectorHeap(entries)
    // build mst recursively
    val mst     = recHeapPrim(heap, graph, Map.empty)
    mst
  }

  @tailrec
  private def recNaiveKruskal(graph: Graph, edges: Seq[Edge]): Graph =
    if (edges.isEmpty) graph
    else {
      if (isCyclic(graph, edges.head)) { // O(n)
        // if the graph is cyclic then ignore the selected edge
        recNaiveKruskal(graph, edges.tail)
      } else {
        // otherwise add the edge to the graph
        recNaiveKruskal(Graph(graph.vertices :+ edges.head.v, graph.edges :+ edges.head), edges.tail)
      }
    }

  def naiveKruskal(graph: Graph): Graph = {
    val graphAfterSort = sortedGraph(graph)                                 // build a graph with sorted edges O(n * log n)
    val bufferGraph    = Graph(Nil, Nil)                                    // used to check if cyclic O(k)
    val mst            = recNaiveKruskal(bufferGraph, graphAfterSort.edges) // O(n * n)
    mst
  }
}
