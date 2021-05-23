package example.struct

import scala.annotation.tailrec

case class Node(parent: Option[Int], treeSize: Int)

class UnionFind private (nodes: Vector[Node]) {
  def union(v1: Int, v2: Int): UnionFind =
    if (v1 != v2) {
      val root1 = root(v1)
      val root2 = root(v2)
      if (root1 == root2) this
      else {
        val node1       = nodes(root1)
        val node2       = nodes(root2)
        val newTreeSize = node1.treeSize + node2.treeSize
        val (newNode1, newNode2) = {
          // size j > size i
          if (node1.treeSize < node2.treeSize) {
            (Node(Some(v2), newTreeSize), Node(node2.parent, newTreeSize))
          } else {
            (Node(node1.parent, newTreeSize), Node(Some(v1), newTreeSize))
          }
        }
        new UnionFind(nodes.updated(root1, newNode1).updated(root2, newNode2))
      }
    } else {
      this
    }

  def find(v1: Int, v2: Int): Boolean = v1 == v2 || root(v1) == root(v2)

  @tailrec
  private def root(v: Int): Int = nodes(v).parent match {
    case Some(parent) => root(parent)
    case None         => v
  }

}

object UnionFind {
  def create(size: Int): UnionFind = {
    val nodes = Vector.fill(size)(Node(None, 1))
    new UnionFind(nodes)
  }
}