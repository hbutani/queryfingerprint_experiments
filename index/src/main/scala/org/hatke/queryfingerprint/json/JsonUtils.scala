package org.hatke.queryfingerprint.json

import org.json4s.JsonAST.JString
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, ShortTypeHints}
import scala.language.implicitConversions

object JsonUtils {

  val jsonFormat : Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all


  def asJson(v: Any)(implicit formats: Formats = jsonFormat): String = {
    pretty(render(Extraction.decompose(v)))
  }

  def fromJson[T: Manifest](s: String)(implicit formats: Formats = jsonFormat): T = {
    parse(s).extract[T]
  }

}
