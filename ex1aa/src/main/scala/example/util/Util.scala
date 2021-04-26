package example.util

object Util {
  def time[R](block: => R): (Double, R) = {
    val t0      = System.nanoTime()
    val result  = block
    val t1      = System.nanoTime()
    val elapsed = BigDecimal(t1 - t0).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble / Math.pow(10, 6)
    (elapsed, result)
  }
}
