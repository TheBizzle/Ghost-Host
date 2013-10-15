package org.bizzle.plugin.ghosthost

import
  java.{ io, net },
    io.IOException,
    net.URL

import
  sbt.{ Classpaths, Compile, ModuleID, UpdateReport }

class ModuleSpec(val moduleID: ModuleID, paths: Seq[String]) {

  private[ghosthost] def getPathMappings: Map[String, String] = {
    import moduleID.{ name => libName, organization => org, revision => versionStr }
    val webJarsPrefix = "META-INF/resources/webjars"
    val mappings =
      if (org == "org.webjars")
        paths map {
          case path if path.startsWith("/") || path.startsWith(webJarsPrefix) =>
            path -> s"$libName/$path"
          case path =>
            s"$webJarsPrefix/$libName/$versionStr/$path" -> s"$libName/$path"
        }
      else
        paths map (path => path -> s"$libName/$path")
    mappings.toMap
  }

  def findURL()(implicit update: UpdateReport): URL =
    Classpaths.managedJars(Compile, Set("jar"), update) map (_.data) find {
      file =>
        val str = file.toString
        (moduleID.organization split '.' forall str.contains) && (str contains moduleID.name)
    } map {
      _.toURI.toURL
    } getOrElse (
      throw new IOException(s"Required '.jar' missing for the following `ModuleSpec`: $this")
    )

}

object ModuleSpecs {

  implicit class EnhancedModuleID(val moduleID: ModuleID) {
    def usingFilesAt(paths: String*) = new ModuleSpec(moduleID, paths)
  }

  implicit def moduleSpec2ModuleID(spec: ModuleSpec): ModuleID = spec.moduleID

}
