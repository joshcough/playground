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
    val json = Color("CadetBlue", RGB(95, 158, 160)).toJson
    println(List(
      ("json", json)
     ,("json.convertTo[Color]", json.convertTo[Color])
     ,("round-trip", json.convertTo[Color].toJson)
     ,("round-trip equality", json.convertTo[Color].toJson == json)
    ).mkString("\n", "\n\n", "\n"))
  }
}
