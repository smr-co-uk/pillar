package com.chrisomeara.pillar

import java.io.BufferedInputStream
import java.io.InputStream
import scala.collection.JavaConversions.asScalaBuffer

/**
 * Pillar Registry factory the loads migration definitions from the classpath.
 * This will load definitions from files and jar resources.
 * 
 * @author Peter Lappo
 */

object ClassPathRegistry {
  def apply(migrations: Seq[Migration]): Registry = {
    new Registry(migrations)
  }

  def fromClassPath(clazz: Class[_], path: String, reporter: Reporter): Registry = {
    new Registry(parseMigrationsInClassPath(clazz, path).map(new ReportingMigration(reporter, _)))
  }

  def fromClassPath(clazz: Class[_], path: String): Registry = {
    new Registry(parseMigrationsInClassPath(clazz, path))
  }

  private def parseMigrationsInClassPath(clazz: Class[_], path: String): Seq[Migration] = {
    import scala.collection.JavaConversions.asScalaBuffer
    val paths = ClassPathUtil.getResourceListing(clazz, path)
    val parser = Parser()
    paths.map {
      path =>
        val stream:InputStream = new BufferedInputStream(path.openStream())
        try {
          parser.parse(stream)
        } finally {
          stream.close()
        }
    }.toList
  }

}
