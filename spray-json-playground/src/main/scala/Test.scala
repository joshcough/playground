import spray.json._
import DefaultJsonProtocol._ 

/** 
 * What if you have json that isn't super well structured.
 *
 * For example this color object:
 *   {"CadetBlue": {"red":95,"green":158,"blue":160}}
 *
 * Where the name shows up as a key in the object, 
 * instead of having a "name" key, with a corresponding value
 *
 * This file shows one way to solve that problem. There may be better solutions.
 */ 

case class RGB(red: Int, green: Int, blue: Int)
case class Color(name: String, rgb: RGB)

object MyJsonProtocol extends DefaultJsonProtocol {
  // rgb is super simple
  implicit val rgbFormat = jsonFormat3(RGB)
  // color isn't so easy
  implicit object ColorJsonFormat extends RootJsonFormat[Color] {
    def write(c: Color) = JsObject(c.name -> c.rgb.toJson)
    def read(value: JsValue) = value match {
      case JsObject(m) => m.toList match {
        case List((name, rgb)) => new Color(name, rgb.convertTo[RGB])
        case _ => deserializationError("Color expected")
      } 
      case _ => deserializationError("Color expected")
    }
  }
}

object Main {
  import MyJsonProtocol._
  def main(args: Array[String]): Unit = {
    val colorJson = Color("CadetBlue", RGB(95, 158, 160)).toJson
    println(List(
      ("colorJson", colorJson)
     ,("colorJson.convertTo[Color]", colorJson.convertTo[Color])
     ,("round-trip", colorJson.convertTo[Color].toJson)
     ,("round-trip equality", colorJson.convertTo[Color].toJson == colorJson)
    ).mkString("\n", "\n\n", "\n"))
  }
}
