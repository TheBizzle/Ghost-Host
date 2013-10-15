package org.bizzle.plugin.ghosthost

import
  java.{ io, net },
    io.IOException,
    net.URLClassLoader

import
  scala.io.Source

import
  sbt.{ File, IO, Logger, richFile, stringToProcess, TestsFailedException, UpdateReport }

object TestRunner {

  def apply(config: GhostHostConfig, debug: DebugSettings, specs: Seq[ModuleSpec])
           (implicit logger: Logger, update: UpdateReport): Unit = {
    logger.info(s"Running tests headlessly with PhantomJS v${findVersionStr()}...")
    IO.withTemporaryDirectory {
      tempDir =>
        val (htmlFile, qunitBootstrap) = moveFiles(tempDir, config, specs)
        if (debug.enabled)
          debug.action(htmlFile.getAbsolutePath)
        else
          runPhantomJS(qunitBootstrap.getAbsolutePath, htmlFile.getAbsolutePath)
    }
  }

  private def moveFiles(tempDir: File, config: GhostHostConfig, specs: Seq[ModuleSpec])
                       (implicit update: UpdateReport): (File, File) = {

    val writeResourceToPath = (classLoader: ClassLoader) => (resourcePath: String, filePath: String) => {

      val rsrc  = classLoader.getResourceAsStream(resourcePath)
      val src   = Source.fromInputStream(rsrc)
      val lines = src.getLines().toList
      src.close()

      val newFile = tempDir / filePath
      newFile.getParentFile.mkdirs()
      newFile.createNewFile()
      IO.writeLines(newFile, lines)

      newFile

    }

    val writeJSToPath = (path: String) => writeResourceToPath(this.getClass.getClassLoader)(s"javascript/$path", path)

    val htmlFile = tempDir / "test.html"
    htmlFile.createNewFile()
    IO.write(htmlFile, config.toHtmlStr)

    val qunitBootstrap = writeJSToPath("run-qunit.js")

    Seq("require.js", "qunit.js", "qunit.css") foreach writeJSToPath

    val urls   = specs map (_.findURL()) toArray
    val loader = URLClassLoader.newInstance(urls, this.getClass.getClassLoader)

    val writeManagedToPath: ((String, String)) => File = {
      case (resourcePath, filePath) => writeResourceToPath(loader)(resourcePath, s"managed/$filePath")
    }

    specs map (_.getPathMappings) foreach (_ foreach writeManagedToPath)

    IO.copyDirectory(config.baseDir, tempDir)

    (htmlFile, qunitBootstrap)

  }

  private def runPhantomJS(phantomPath: String, htmlPath: String)(implicit logger: Logger): Unit = {

    val result = s"phantomjs $phantomPath $htmlPath".lines_!

    val TestRegex =
      """
        |(?s)(.*?)
        |Tests completed in \d+ milliseconds\.
        |(\d+) assertions of (\d+) passed, (\d+) failed\.
      """.trim.stripMargin.r

    result.mkString("\n") match {
      case TestRegex(extra, assertions, successes, failures) =>
        val status = s"$assertions Attempted, $successes Passed, $failures Failed"
        if (failures == "0")
          logger.info("All tests passed: " + status)
        else {
          logger.error(s"$failures test(s) failed: $status\n$extra")
          throw new TestsFailedException
        }
      case _ =>
        logger.warn(s"Unexpected output from QUnit runner:\n${result map (x => s"> $x") mkString "\n"}")
    }

  }

  private def findVersionStr()(implicit logger: Logger): String =
    try "phantomjs --version".!!.init
    catch {
      case ex: IOException =>
        logger.error("PhantomJS must be installed and on your $PATH in order to run QUnit tests!")
        throw new TestsFailedException
    }

}
