import java.io._

import scala.io._

object Build {
  val $ = Source.fromFile(new File("../version.properties")).getLines().filter(_.split("=").size == 2).map(_.split("=") match {
    case Array(k, v) => k.trim -> v.trim
  }).toMap
}