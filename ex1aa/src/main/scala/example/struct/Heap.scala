package example.struct

/** Key-Value pair entry */
case class Entry[K, V](key: K, value: V)

/** Immutable Heap definition */
trait Heap[K, V] {
  def size: Int
  def isEmpty: Boolean

  /** Insert the given key-value pair into the heap
    * @param key the key
    * @param value the value
    * @return A [[Heap]] with the given value inserted
    */
  def insert(key: K, value: V): Heap[K, V]

  /** Remove the smallest element from the heap sorted by key
    * @return a pair of the removed [[Entry]] and the new [[Heap]]
    */
  def extractMin: (Option[Entry[K, V]], Heap[K, V])

  /** Update the key for the given value.
    * If the given key is >= than the current one,
    * no work is done and the heap is left unchanged.
    */
  def decreaseKey(value: V, newKey: K): Heap[K, V]
}
