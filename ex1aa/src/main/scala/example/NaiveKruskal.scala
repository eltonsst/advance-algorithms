package example

import breeze.plot.{plot, Figure}
import com.typesafe.scalalogging.LazyLogging
import example.graph.Graph.{loadFromFile, numVertices}
import example.graph.MST.naiveKruskal
import example.util.Util.time

import scala.collection.mutable.ArrayBuffer

object NaiveKruskal extends LazyLogging {

  def runKruskal(): Unit = {
    val buffer    = ArrayBuffer[(Double, Int)]()
    val totalTime = time {
      loadFromFile().foreach { graph =>
        val numV = numVertices(graph)
        val mst = time(naiveKruskal(graph))
        logger.info(s"num vertices of mst: ${numV}, time: ${mst._1} ms")
        logger.info(s"mst weight ${mst._2.edges.map(_.w).sum}")
        buffer.append((mst._1, numV))
      }
    }

    logger.info(s"Total time execution: ${totalTime._1} ms")

    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt += plot(buffer.map(_._2.toDouble).toSeq, buffer.map(_._1).toSeq)
    heapFigure.saveas("kruskal-naive-plot.pdf")
  }

  def draw = {
    val x = Seq(10, 10, 10, 10, 20,20,20,20,40,40,40,40,80,80,80,80,100,100,100,100,200,200,200,200,400,400,400,400,800,800,800,800,1000,1000,1000,1000,2000,2000,2000,2000,4000,4000,4000,4000,8000,8000,8000,8000, 10000,10000,10000,10000, 20000,20000,20000,20000, 40000,40000,40000,40000, 80000,80000,80000,80000, 100000,100000,100000,100000)
    val y = Seq(7.785287,0.8568,0.981246,0.814859, 3.209113,2.184996,2.022803,1.226059,4.654827,5.678661,7.26434,2.325569,9.855143,9.715726,7.386184,
      6.918087, 10.614078,28.785867,8.556119,7.428554,29.214195,  21.200612,
      21.577769,21.499067,  113.990006,82.170466, 86.721979, 84.763555, 333.503695, 400.962994, 364.938107, 339.11464, 599.239539, 594.788929, 600.084295,605.389506, 2527.692328, 2562.509183,
      2511.069803, 2562.970662, 10768.09383, 12439.81076, 10933.900036, 11062.575874,52998.287343, 49228.786561, 50428.808548,  49019.883195, 78007.76378,79700.073263,8209.288563, 75599.810123,
      475168.802433, 508077.820715, 447511.548203,356875.301677,  1840256.882036, 1889193.892009, 1916549.145976, 1896923.968475, 10020000.4343, 10020000.4343,10020000.4343,10020000.4343, 15146597.8771, 15146597.8771,15146597.8771,15146597.8771)
    val heapFigure = Figure()
    heapFigure.visible = false
    val plt        = heapFigure.subplot(0)
    plt.xlabel = "size"
    plt.ylabel = "time ms"
    plt += plot(x.map(_.toDouble), y.map(_.toDouble))
    heapFigure.saveas("kruskal-naive-plot.pdf")
  }

  def main(args: Array[String]): Unit = draw

}
