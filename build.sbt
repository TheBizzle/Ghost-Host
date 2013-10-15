sbtPlugin := true

name := "ghost-host"

organization := "org.bizzle"

version := "1.0"

val moveJS = taskKey[Unit]("Moves JS files")

moveJS <<= (classDirectory in Compile) map { c => moveThatJS(c) }

packageBin in Compile <<= (packageBin in Compile).dependsOn(moveJS).dependsOn(compile in Compile)
