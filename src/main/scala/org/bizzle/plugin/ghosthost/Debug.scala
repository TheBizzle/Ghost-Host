package org.bizzle.plugin.ghosthost

import
  sbt.{ Logger, stringToProcess }

case class DebugSettings(enabled: Boolean = false, action: DebugAction = RunCommand(arg => s"google-chrome $arg"))

trait DebugAction {

  protected def performExtra(htmlPath: String)(implicit logger: Logger): Unit = {}

  final def apply(htmlPath: String)(implicit logger: Logger): Unit = {
    logger.info("HTML file available at " + htmlPath)
    logger.info("Halting... (press 'return' to resume)")
    performExtra(htmlPath)
    readLine()
    logger.info("Unpaused.  Have a wonderful day!")
  }

}

case object JustPause extends DebugAction
case class RunCommand(template: (String) => String) extends DebugAction {
  override def performExtra(htmlPath: String)(implicit logger: Logger): Unit =
    template(htmlPath).!
}

