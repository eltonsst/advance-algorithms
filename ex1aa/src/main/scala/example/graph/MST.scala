package example.graph

import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph._
import example.struct.{Entry, Heap, UnionFind, VectorHeap}

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec

/** Functions to create a minimum spanning tree for undirected weighted graphs.
  */
object MST extends LazyLogging {
  var counter = new AtomicInteger(0)
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
      counter.incrementAndGet()
      if (heap.exist(edgesAdjacentToU._2.head.v).isDefined && edgesAdjacentToU._2.head.w < heap.key(edgesAdjacentToU._2.head.v)) {

        val updatedMst =
          Graph(mst.vertices, edgesAdjacentToU._2.head +: mst.edges)                //mst + (edgesAdjacentToU._1 -> edgesAdjacentToU._2.head.v)
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
      val (maybeU, heapAfterExtraction)     = heap.extractMin                                        //(log n)
      // get list of vertex v adjacent to u
      val edgesAdjacentToU                  = getAdjacencyList(graph, maybeU.get.value)              // O(eC)
      // update state of the heap and current MST
      val (mstAfterUpdate, heapAfterUpdate) = updateHeap(edgesAdjacentToU, heapAfterExtraction, mst) // O(m * log(n)
      recHeapPrim(heapAfterUpdate, graph, mstAfterUpdate)
    }

  def heapPrim(graph: Graph, s: Int = 1): Graph = {
    // set key of initial node to 0, infinite for the others
    val entries = graph.vertices.map(v => if (v == s) Entry(0, v) else Entry(Int.MaxValue, v)) // O(numVertices)
    // heap is the priority queue, contains all nodes not in the tree
    val heap    = VectorHeap(entries)                                                          // O(numVertices * log numVertices)
    // build mst recursively
    val mst     = recHeapPrim(heap, graph, Graph(graph.vertices, Nil))                         // O(numVertices * degree(v) * log(numVertices))
    logger.info(s"counter is ${counter.get()}")
    counter.set(0)
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
        recNaiveKruskal(Graph(edges.head.v +: graph.vertices, edges.head +: graph.edges), edges.tail)
      }
    }

  def naiveKruskal(graph: Graph): Graph = {
    val graphAfterSort = sortedGraph(graph)                               // TimSort O(numEdges * log(numEdges))
    val bufferMst      = Graph(1 :: Nil, Nil)                             // used to check if cyclic O(k)
    val mst            = recNaiveKruskal(bufferMst, graphAfterSort.edges) // O(numEdges * numVertices)
    mst
  }

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
    val graphAfterSort  = sortedGraph(graph)                   // TimSort O(numEdges * log(numEdges))
    val bufferMst       = Graph(graph.vertices, Nil)                 // used to check if cyclic O(k)
    val unionFindStruct = UnionFind.create(numVertices(graph) + 1) // O(numVertices)
    recUnionFindKruskal(unionFindStruct, bufferMst, graphAfterSort.edges)
  }
}
