package org.bizzle.plugin.ghosthost

import
  sbt.{ Compile, Keys, Plugin => SBTPlugin, SettingKey, TaskKey },
    Keys._

object Plugin extends SBTPlugin {

  object GhostHost {

    val ghConfig      = SettingKey[GhostHostConfig]("ghost-host-config", "The `GhostHostConfig` that will be used to determine how to load your JS files")
    val debugSettings = SettingKey[DebugSettings]  ("debug-settings",    "The bundle of debugging settings")
    val moduleSpecs   = SettingKey[Seq[ModuleSpec]]("module-specs",      "The `ModuleSpec` objects denoting which managed files to extract.  Files are extracted to './managed/<libraryName>/<providedPath>.  WebJar paths can be supplied relative to the <versionNumber> folder")

    val testJS = TaskKey[Unit]("test-js", "Run JavaScript tests (requires a local installation of PhantomJS")

    val settings = Seq(
      debugSettings := DebugSettings(),
      testJS <<= (ghConfig, debugSettings, moduleSpecs, update, streams, compile in Compile) map {
        (config, debug, specs, update, s, _) => TestRunner(config, debug, specs)(s.log, update)
      },
      moduleSpecs := Seq()
    )

  }

}
