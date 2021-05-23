package example.graph

import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt
import scala.util.Try


case class TimerException(value: Int) extends Exception(value.toString)

object TSP {
  var t0: Long = System.nanoTime()

  @tailrec
  private def doNearestNeighbor(graph: Graph, s: Int, tsp: Graph): Graph = {
    if(graph.edges.isEmpty) tsp
    else {
      val verticesInTsp = tsp.vertices.map(v => (v, v)).toMap
      val nearest = graph.edges
        // find nearest vertices not already inserted
         .filter(e =>
          (e.u == s && !verticesInTsp.contains(e.v))
            || (e.v == s) && !verticesInTsp.contains(e.u)
        )
        // take the smallest Vk+1 in term of cost
        .minByOption(_.w)
      if(nearest.isDefined) {
        val newS = if(s == nearest.get.v) nearest.get.u else nearest.get.v
        val newTsp = Graph(newS +: tsp.vertices, nearest.get +: tsp.edges)
        val newGraph = Graph(graph.vertices, graph.edges.filter(_ != nearest.get))
        doNearestNeighbor(newGraph, newS, newTsp)
      } else {
        tsp
      }
    }
  }


  def nearestNeighbor(graph: Graph): Graph =
    doNearestNeighbor(graph, 1, Graph(1 :: Nil, Nil))



  def doHeldKarp(graph: Graph, v: Int, S: Seq[Int], d: scala.collection.mutable.Map[(Int, Seq[Int]), Int]): Int = {
    S match {
      case s::Nil if s == v => graph.edges.filter(e => e.u == 1 && e.v == v).map(_.w).head
      case _ =>
        if(d.contains((v, S))) d(v, S)
        else {
          val sMinusV = S.filter(_ != v)
          var minDist = Int.MaxValue
          sMinusV.foreach(u => {
            val dist = doHeldKarp(graph, u, sMinusV, d)
            val maybeMinDist =
              dist + graph.edges.find(e => e.u == v && e.v == u).map(_.w)
                .getOrElse(graph.edges.find(e => e.u == u && e.v == v).map(_.w).get)
            if(maybeMinDist < minDist) minDist = maybeMinDist
            val stop = (System.nanoTime() - t0) > 30.seconds.toNanos
            //System.out.println(s"$stop for ${(System.nanoTime() - t0) / 1000000000}  >  ${10.seconds}")
            if(stop) {
              throw TimerException(minDist)
            }
          })
          d.update((v, S), minDist)
          minDist
        }
    }
  }

  def heldKarp(graph: Graph): Int = {
    t0 = System.nanoTime()
    val hkDist =
      Try{
        doHeldKarp(graph, 1, graph.vertices, scala.collection.mutable.Map.empty)
      }.recover{
        case e: TimerException => e.value
      }

    hkDist.get
  }
}
