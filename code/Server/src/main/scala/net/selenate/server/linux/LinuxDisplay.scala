package net.selenate.server
package linux

import com.ferega.procrun._
import scala.annotation.tailrec

case class DisplayInfo(num: Int, port: Int)

object LinuxDisplay {
  private val log = Log(LinuxDisplay.getClass)
  private val BaseNum = 200

  def create(): DisplayInfo = {
    val num = getFirstFree()
    create(num)
  }

  def destroy(num: Int) {
//    runPkill(s"x11vnc.*:$num")
    runPkill(s"icewm.*:$num")
    runPkill(s"Xvfb.*:$num")
  }

  def destroyAll() {
//    runPkill(s"x11vnc.*")
    runPkill(s"icewm.*")
    runPkill(s"Xvfb.*")
  }

  val PortR = """PORT=(\d+)"""r
  private def create(num: Int): DisplayInfo = {
    log.info("Creting new display for display number[:" + num + "]")
    val result = for {
      xvfb   <- runXvfb(num).right
      iceWM  <- runIceWM(num).right
      x11vnc <- runX11vnc(num).right
    } yield {
//      val out = x11vnc.stdOutSoFar.trim
//      log.trace("X11vnc.stdOutSoFar.trim output = " +  out)
//
//      val PortR(port) = out
//      port.toInt
      0
    }

    result match {
      case Left(msg)   => throw new Exception(s"An error occured while creating screen $num:\n$msg")
      case Right(port) => DisplayInfo(num, port)
    }
  }

  private def runXvfb(num: Int)         = {
    log.trace("Xvfb for number[:" + num + "]")
    val res = LinuxProc.runAndVerify("Xvfb", s":$num" | "-screen" | "0" | "1024x768x16" | "-ac")
    log.trace("Xvfb run result = " +  res)
    res
  }
  private def runIceWM(num: Int)        = {
    log.trace("IceWM for number[:" + num + "]")
    val res = LinuxProc.runAndVerify("icewm", Seq(s"--display=:$num"))
    log.trace("IceWM run result = " +  res)
    res
  }
  private def runX11vnc(num: Int)       = {
    log.trace("X11vnc for number[:" + num + "]")
    val res = LinuxProc.runAndVerify("x11vnc", "-display" | s":$num" | "-listen" | "localhost" | "-nopw" | "-xkb" | "-shared" | "-forever")
    log.trace("X11vnc run result = " +  res)
    res
  }
  private def runXdpyInfo(num: Int)     = LinuxProc.runAndEnd("xdpyinfo", "-display" | s":$num")
  private def runPkill(pattern: String) = LinuxProc.runAndEnd("pkill", "-f" | pattern)

  @tailrec
  private def getFirstFree(num: Int = BaseNum): Int = {
    log.trace("Trying to create display for %d" format num)
    if (isFree(num)) num else getFirstFree(num+1)
  }

  private case object Lock
  private val screenStatusBuffer: scala.collection.mutable.Set[Int] = scala.collection.mutable.Set.empty
  private def isFree(num: Int) = {
    Lock.synchronized {
      if (screenStatusBuffer.contains(num)) {
        false
      } else {
        screenStatusBuffer += num
        true
      }
    }
//    val free = runXdpyInfo(num).contains("unable to open display")
//    val freeMsg = if(free) "" else " not"
//    log.trace("Display [%s] is%s free!" format (num, freeMsg))
//    free
  }
}
