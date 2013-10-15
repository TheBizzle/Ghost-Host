import
  scala.io.Source

import
  sbt._,
    Keys._

object PluginBuild extends Build {

  def moveThatJS(classesDir: File): Unit = {

    val paths = Seq(
      "META-INF/resources/webjars/qunit/1.11.0/qunit.css",
      "META-INF/resources/webjars/requirejs/2.1.8/require.js"
    )

    val dest = classesDir / "javascript"
    dest.mkdir()

    paths foreach writeResourceIntoDir(dest)
    handleStupidAutoStartingQUnitLib(dest)

  }

  private def writeResourceIntoDir(dir: File)(path: String): Unit = {
    val (outFile, lines) = generateFileAndLines(dir, path)
    IO.writeLines(outFile, lines)
  }

  private def handleStupidAutoStartingQUnitLib(dir: File): Unit = {

    val path = "META-INF/resources/webjars/qunit/1.11.0/qunit.js"
    val (outFile, lines) = generateFileAndLines(dir, path)

    val StupidAutoStartingQUnitLibSettingRegex = """(\s*autostart:\s*)true(,\s*)""".r
    val newLines = lines map {
      case StupidAutoStartingQUnitLibSettingRegex(key, post) =>
        s"${key}false${post}"
      case x =>
        x
    }

    IO.writeLines(outFile, newLines)

  }

  // Can almost certainly be done better (AKA without loading the whole resource into memory) --JAB (10/13/13)
  private def generateFileAndLines(dir: File, path: String): (File, Seq[String]) = {

    val filename = path.reverse.takeWhile(_ != '/').reverse
    val rsrc     = this.getClass.getClassLoader.getResourceAsStream(path)
    val src      = Source.fromInputStream(rsrc)
    val lines    = src.getLines().toList
    src.close()

    val outFile = dir / filename
    outFile.createNewFile()

    (outFile, lines)

  }

}

