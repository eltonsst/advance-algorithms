package example

import example.graph.Graph._
import breeze.plot._

object Main extends App {

  val g   = loadFromFile("mst_dataset/input_random_02_10.txt")
  val fig = Figure()
  val plt = fig.subplot(0)
  plt += plot((1 :: 2 :: 3 :: 4 :: Nil).map(_.toDouble), (1 :: 2 :: 3 :: 4 :: Nil).map(_.toDouble))
  fig.saveas("test_graph.pdf")
  println(s"numVer = ${numVertices(g)}, numE = ${numEdges(g)}, al= ${g.adjacencyList}")
}
