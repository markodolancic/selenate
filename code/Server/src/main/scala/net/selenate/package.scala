package net.selenate

import server.Loggable

import java.{ util => ju }
import scala.concurrent.ExecutionContext

package object server
    extends RichClasses
    with Extractors with Loggable {
  type PF[A, R] = PartialFunction[A, R]

  implicit val ec = ExecutionContext.fromExecutor(ju.concurrent.Executors.newCachedThreadPool())

  def tryo[T](f: => T): Option[T] =
    try {
      Some(f)
    } catch {
      case e: Exception =>
        logTrace("Error in tryo", e)
        None
    }

  def tryb[T](f: => T): Boolean =
    try {
      f
      true
    } catch {
      case e: Exception =>
        logTrace("Error in tryb", e)
        false
    }

  // scala.collection.JavaConversions and scala.collection.JavaConverters are not adequate.
  // They create instances of scala.collection.JavaConversions$SeqWrapper (or similar), which subclass
  // java lists.
  // That fails when pure java tries to de-serialize it.
  def seqToRealJava[T](in: Seq[T]): ju.List[T] = {
    val out = new ju.ArrayList[T]()
    in foreach out.add
    out
  }

  def setToRealJava[T](in: Set[T]): ju.Set[T] = {
    val out = new ju.HashSet[T]()
    in foreach out.add
    out
  }

  def mapToRealJava[K, V](in: Map[K, V]): ju.Map[K, V] = {
    val out = new ju.HashMap[K, V]
    in foreach { case(k, v) => out.put(k, v) }
    out
  }

  def toInteger(i: Int) = java.lang.Integer.valueOf(i)
}
