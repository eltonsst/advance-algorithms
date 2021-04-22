package example.graph

import example.graph.Graph._
import example.struct.{Entry, Heap, VectorHeap}

import scala.annotation.tailrec

/** Functions to create a minimum spanning tree for undirected weighted graphs.
  */
object MST {
  type MST = Map[Int, Int]

  @tailrec
  private def updateHeap(
      edgesAdjacentToU: (Int, Seq[Edge]),
      heap: Heap[Int, Int],
      mst: Graph
  ): (Graph, Heap[Int, Int]) =
    if (edgesAdjacentToU._2.isEmpty) (mst, heap) // trivial
    else {
      // update the key value
      if (heap.exist(edgesAdjacentToU._2.head.v).isDefined && edgesAdjacentToU._2.head.w < heap.key(edgesAdjacentToU._2.head.v)) {

        val updatedMst = Graph(mst.vertices, edgesAdjacentToU._2.head +: mst.edges) //mst + (edgesAdjacentToU._1 -> edgesAdjacentToU._2.head.v)
        updateHeap(
          (edgesAdjacentToU._1, edgesAdjacentToU._2.tail),
          heap.decreaseKey(edgesAdjacentToU._2.head.v, edgesAdjacentToU._2.head.w), // (log numVertices)
          updatedMst
        )
      } else
        updateHeap((edgesAdjacentToU._1, edgesAdjacentToU._2.tail), heap, mst)
    }

  @tailrec
  private def recHeapPrim(heap: Heap[Int, Int], graph: Graph, mst: Graph): Graph =
    if (heap.isEmpty) mst
    else {
      // let u be the min extracted from the heap
      val (maybeU, heapAfterExtraction)     = heap.extractMin //(eC)
      // get list of vertex v adjacent to u
      val edgesAdjacentToU                  = getAdjacencyList(graph, maybeU.get.value) // O(eC)
      // update state of the heap and current MST
      val (mstAfterUpdate, heapAfterUpdate) = updateHeap(edgesAdjacentToU, heapAfterExtraction, mst) // O(degree(v) * log(numVertices)
      recHeapPrim(heapAfterUpdate, graph, mstAfterUpdate)
    }

  def heapPrim(graph: Graph, s: Int = 1): Graph = {
    // set key of initial node to 0, infinite for the others
    val entries = graph.vertices.map(v => if (v == s) Entry(0, v) else Entry(Int.MaxValue, v)) // O(numVertices)
    // heap is the priority queue, contains all nodes not in the tree
    val heap    = VectorHeap(entries) // O(numVertices * log numVertices)
    // build mst recursively
    val mst     = recHeapPrim(heap, graph, Graph(graph.vertices, Nil)) // O(numVertices * degree(v) * log(numVertices))
    mst
  }

  @tailrec
  private def recNaiveKruskal(graph: Graph, edges: Seq[Edge]): Graph =
    if (edges.isEmpty) graph
    else {
      if (isCyclic(graph, edges.head)) { // O(numVertices)
        // if the graph is cyclic then ignore the selected edge
        recNaiveKruskal(graph, edges.tail)
      } else {
        // otherwise add the edge to the graph, important: prepend on the list!
        recNaiveKruskal(Graph(edges.head.v +: graph.vertices  , edges.head +: graph.edges), edges.tail)
      }
    }

  def naiveKruskal(graph: Graph): Graph = {
    val graphAfterSort = sortedGraph(graph)                                // TimSort O(numEdges * log(numEdges))
    val bufferMst    = Graph(1 :: Nil, Nil)                                // used to check if cyclic O(k)
    val mst            = recNaiveKruskal(bufferMst, graphAfterSort.edges) // O(numEdges * numVertices)
    mst
  }
}
