package example.util

object Util {
  def time[R](block: => R): (Double, R) = {
    val t0      = System.nanoTime()
    val result  = block
    val t1      = System.nanoTime()
    val elapsed = Math.floor((t1 - t0) / Math.pow(10, 6))
    (elapsed, result)
  }
}
