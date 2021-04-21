package example

import breeze.plot.{plot, Figure}
import example.graph.Graph._
import example.graph.MST._
import example.util.Util.time

import scala.collection.mutable.ArrayBuffer

object Main extends App {
  def runPrim(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()
    val totalTime = time {
      loadFromFile("mst_dataset/").foreach { graph =>
        val mst = time(heapPrim(graph))
        println(s"mst keys: ${mst._2.keys.size}, time: ${mst._1} ms")
        buffer.append((mst._1, mst._2.keys.size))
      }
    }

    println(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    val plt        = heapFigure.subplot(0)
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("heap-plot.pdf")
  }

  def runKruskal(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()
    val totalTime = time {
      loadFromFile("mst_dataset/").foreach { graph =>
        val mst = time(naiveKruskal(graph))
        println(s"num vertices: ${numVertices(graph)}, time: ${mst._1} ms")
        buffer.append((mst._1, numVertices(graph)))
      }
    }

    println(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    val plt        = heapFigure.subplot(0)
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("kruskal-naive-plot.pdf")
  }

  // runPrim()

  runKruskal()

}
