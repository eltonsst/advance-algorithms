package example.struct

import scala.annotation.tailrec

class VectorHeap[K: Ordering, V] private (vector: Vector[Entry[K, V]]) extends Heap[K, V] {
  private val keyOrdering = implicitly[Ordering[K]]

  // dummy constructor
  private case class State(vector: Vector[Entry[K, V]])
  // internal state of the heap
  private val state = State(vector)

  private def updated(state: State): VectorHeap[K, V] = new VectorHeap[K, V](state.vector)

  override def size: Int        = vector.length
  override def isEmpty: Boolean = size == 0

  /**
   * Append an entry to the bottom of the heap.
   * ATTENTION: breaking heap rules here!
   * @param entry an entry to be inserted
   * @param state current state of the heap
   * @return the index were the entry was inserted and the new heap state
   */
  private def append(entry: Entry[K, V], state: State): (Int, State) =
    (state.vector.size, State(state.vector :+ entry))

  def hasParent(index: Int): Boolean                                    = index > 0

  def getParentIndex(index: Int): Int = Math.floor((index - 1) / 2).toInt

  // swap element "i" and "j" of the vector and returns the new state
  private def swap(i: Int, j: Int, state: State): State =
    State(state.vector.updated(i, state.vector(j)).updated(j, state.vector(i)))

  @tailrec
  private def bubbleUp(insertedIndex: Int, entry: Entry[K, V], stateAfterAppend: State): State =
    if (hasParent(insertedIndex)) {
      val parentIndex = getParentIndex(insertedIndex)
      val parent      = stateAfterAppend.vector(parentIndex)
      if (keyOrdering.compare(parent.key, entry.key) > 0) {
        // swap until the vector is ordered
        bubbleUp(parentIndex, entry, swap(insertedIndex, parentIndex, stateAfterAppend))
      } else {
        stateAfterAppend
      }
    } else {
      stateAfterAppend
    }

  /** Insert the given key-value pair into the heap
    *
    * @param key the key
    * @param value the value
    * @return A [[Heap]] with the given value inserted
    */
  override def insert(key: K, value: V): Heap[K, V] = {
    val entry                             = Entry(key, value)
    // breaking heap property
    val (insertedIndex, stateAfterAppend) = append(entry, state)
    // restoring heap property
    val stateAfterBubbling                = bubbleUp(insertedIndex, entry, stateAfterAppend)
    updated(stateAfterBubbling)
  }

  private def heapPropertyViolated(parentKey: K, childIndices: Seq[Int], vector: Vector[Entry[K, V]]): Boolean = {
    val childSmallerThanParent = childIndices.find(index => keyOrdering.compare(parentKey, vector(index).key) > 0)
    childSmallerThanParent.isDefined
  }

  private def getChildIndices(index: Int, vector: Vector[_]): Seq[Int] =
    Seq(index * 2 + 1, index * 2 + 2).filter(_ < vector.size)

  @tailrec
  private def bubbleDown(index: Int, entry: Entry[K, V], stateAfterRemove: State): State = {
    val childIndices = getChildIndices(index, stateAfterRemove.vector)
    if (heapPropertyViolated(entry.key, childIndices, stateAfterRemove.vector)) {
      val minChildIndex  = childIndices.minBy(stateAfterRemove.vector(_).key)
      val stateAfterSwap = swap(index, minChildIndex, stateAfterRemove)
      bubbleDown(minChildIndex, entry, stateAfterSwap)
    } else {
      // no damage of the heap structure founded
      stateAfterRemove
    }
  }

  /** Remove the smallest element from the heap sorted by key
    *
    * @return a pair of the removed [[Entry]] and the new [[Heap]]
    */
  override def extractMin: (Option[Entry[K, V]], Heap[K, V]) = {
    if (isEmpty) {
      (None, this)
    } else {
      // Swap the root and the last leaf
      val stateAfterSwap = swap(0, vector.size - 1, state)

      // Remove the old root, which is now at the end of the array
      val (root, stateAfterRemove) = delete(vector.size - 1, stateAfterSwap)

      // Bubble-down the new root until heap property is restored
      val stateAfterBubbleDown = {
        val oldVector = stateAfterRemove.vector
        if (oldVector.nonEmpty)
          bubbleDown(0, oldVector(0), stateAfterRemove)
        else
          stateAfterRemove
      }

      // Return the old root
      (Some(root), updated(stateAfterBubbleDown))
    }
  }

  private def delete(index: Int, oldState: State): (Entry[K, V], State) = {
    val removedEntry = oldState.vector(index)
    (removedEntry, State(oldState.vector.patch(index, Nil, 1)))
  }
}

object VectorHeap {
  /**
   * Create a heap containing the given entries
   */
  def apply[K : Ordering, V](entries: Seq[Entry[K, V]]): Heap[K, V] = {
    entries.foldLeft[Heap[K, V]] (new VectorHeap(Vector[Entry[K,V]]())) {
      case (heap, entry) => heap.insert(entry.key, entry.value)
    }
}
}
