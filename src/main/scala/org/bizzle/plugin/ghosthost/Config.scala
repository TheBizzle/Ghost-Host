package org.bizzle.plugin.ghosthost

import
  sbt.File

trait GhostHostConfig {

  def baseDir:   File
  def extrasStr: String

  def toHtmlStr: String =
    s"""
       |<!DOCTYPE html>
       |<html>
       |  <head>
       |    <title>JavaScript Tests</title>
       |  </head>
       |  <body>
       |    <link rel="stylesheet" media="screen" href="./qunit.css">
       |    <div id="qunit"></div>
       |    <div id="qunit-fixture"></div>
       |    <script src='./qunit.js' type="text/javascript"></script>
       |    $extrasStr
       |  </body>
       |</html>
     """.trim.stripMargin

}

case class JSListConfig(override val baseDir: File, files: File*) extends GhostHostConfig {
  override def extrasStr =
    files map (f => s"    <script src='file://${f.getAbsolutePath}' type='text/javascript'></script>") mkString "\n"
}

case class RequireJSConfig(override val baseDir: File, configFile: File) extends GhostHostConfig {
  override def extrasStr =
    s"<script type='text/javascript' data-main='file://${configFile.getAbsolutePath}' src='./require.js'></script>"
}
