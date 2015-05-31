import spray.json._
//import DefaultJsonProtocol._
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter, DateTimeFormat}
import scala.reflect.runtime.{currentMirror => m, universe => ru}
import scalaz._
import Scalaz._
import Isomorphism.<=>

object Test2 {

  case class Location(city: String, country: String, region: String)
  case class Units(distance: String, pressure: String, speed: String, temperature: String)

  case class UntypedWind(chill: String, direction: String, speed: String)
  case class Wind(chill: Int, direction: Int, speed: Int)
  implicit val windlIso = new (UntypedWind <=> Wind) {
    val to: UntypedWind => Wind = g => Wind(g.chill.toInt, g.direction.toInt, g.speed.toInt)
    val from: Wind => UntypedWind = g => UntypedWind(g.chill.toString, g.direction.toString, g.speed.toString)
  }

//  case class UntypedAtmosphere(humidity: String, pressure: String, rising: String, visibility: String)
//  case class Atmosphere(umidity: Int, pressure: Double, rising: Int, visibility: Int)
//  implicit val atmospherelIso = new (UntypedAtmosphere <=> Atmosphere) {
//    val to: UntypedAtmosphere => Atmosphere = g => Atmosphere(g.humidity.toInt, g.pressure.toDouble, g.rising.toInt, g.visibility.toInt)
//    val from: Atmosphere => UntypedAtmosphere = g => UntypedAtmosphere(g.chill.toString, g.direction.toString, g.speed.toString)
//  }


  case class UntypedGuid(isPermaLink: String, content: String)
  case class Guid(isPermaLink: Boolean, content: String)
  implicit val guidlIso = new (UntypedGuid <=> Guid) {
    val to: UntypedGuid => Guid = g => Guid(g.isPermaLink.toBoolean, g.content)
    val from: Guid => UntypedGuid = g => UntypedGuid(g.isPermaLink.toString, g.content)
  }

  case class UntypedForecast(code: String, date: String, day: String, high: String, low: String, text: String)
  case class Forecast (code: Int, date: DateTime, day: String, high: Int, low: Int, text: String)
  implicit val forecastIso = new (UntypedForecast <=> Forecast) {
    val d = DateTimeFormat.forPattern("dd MMM yyyy")
    val to: UntypedForecast => Forecast = u => Forecast(
      u.code.toInt, d.parseDateTime(u.date), u.day, u.high.toInt, u.low.toInt, u.text)
    val from: Forecast => UntypedForecast = u => {
      val date: String = d.print(u.date)
      UntypedForecast(u.code.toString, if(date.startsWith("0")) date.drop(1) else date, u.day, u.high.toString, u.low.toString, u.text)
    }
  }

  case class UntypedCondition(code: String, date: String, temp: String, text: String)
  case class Condition(code: Int, date: DateTime, temp: Int, text: String)
  implicit val conditionlIso = new (UntypedCondition <=> Condition) {
    val d = DateTimeFormat.forPattern("EEE, dd MMM yyyy h:mm aa zzz")
    val to: UntypedCondition => Condition = g => Condition(g.code.toInt, d.parseDateTime(g.date), g.temp.toInt, g.text)
    val from: Condition => UntypedCondition = g =>
      UntypedCondition(g.code.toString, d.print(g.date), g.temp.toString, g.text)
  }

  case class UntypedUrl(executionStartTime: String, executionEndTime: String, executionTime: String, content: String)
  case class Url(executionStartTime: Int, executionEndTime: Int, executionTime: Int, content: String)
  implicit val urlIso = new (UntypedUrl <=> Url) {
    val to: UntypedUrl => Url = u => Url(
      u.executionStartTime.toInt, u.executionEndTime.toInt, u.executionTime.toInt, u.content)
    val from: Url => UntypedUrl = u => UntypedUrl(
      u.executionStartTime.toString, u.executionEndTime.toString, u.executionTime.toString, u.content)
  }

  case class UntypedDiagnostics(publiclyCallable: String, url: UntypedUrl, userTime: String, serviceTime: String, buildVersion: String)
  case class Diagnostics(publiclyCallable: Boolean, url: Url, userTime: Int, serviceTime: Int, buildVersion: String)
  implicit val diagIso = new (UntypedDiagnostics <=> Diagnostics) {
    val to: UntypedDiagnostics => Diagnostics = d => Diagnostics(
      d.publiclyCallable.toBoolean, urlIso.to(d.url), d.userTime.toInt, d.serviceTime.toInt, d.buildVersion)
    val from: Diagnostics => UntypedDiagnostics = d => UntypedDiagnostics(
      d.publiclyCallable.toString, urlIso.from(d.url), d.userTime.toString, d.serviceTime.toString, d.buildVersion.toString)
  }

  object MyJsonProtocol extends DefaultJsonProtocol {

    /**
     * {
     *  "city": "Sunnyvale",
     *  "country": "United States",
     *  "region": "CA"
     * }
     */
    implicit val locationFormat = jsonFormat3(Location)

    /**
     * {
     *   "distance": "mi",
     *   "pressure": "in",
     *   "speed": "mph",
     *   "temperature": "F"
     * }
     */
    // don't really have any additional info on units to break it down more.
    implicit val unitsFormat = jsonFormat4(Units)

    /**
     * {
     *   "isPermaLink": "false",
     *   "content": "USCA1116_2013_11_09_7_00_PST"
     * }
     */
    implicit val untypedGuidFormat : JsonFormat[UntypedGuid] = jsonFormat(UntypedGuid, "isPermaLink", "content")
    implicit val guidFormat : JsonFormat[Guid] = isoFormat(untypedGuidFormat, guidlIso)

    /**
     * {
     *  "chill": "61",
     *  "direction": "0",
     *  "speed": "0"
     * }
     */
    implicit val untypedwindFormat : JsonFormat[UntypedWind] = jsonFormat(UntypedWind, "chill", "direction", "speed")
    implicit val windFormat : JsonFormat[Wind] = isoFormat(untypedwindFormat, windlIso)

    /**
     * {
     *   "execution-start-time": "0",
     *   "execution-stop-time": "106",
     *   "execution-time": "106",
     *   "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
     * }
     */
    implicit val untypedUrlFormat : JsonFormat[UntypedUrl] = jsonFormat(UntypedUrl,
      "execution-start-time", "execution-stop-time", "execution-time", "content")
    implicit val urlFormat : JsonFormat[Url] = isoFormat(untypedUrlFormat, urlIso)

    /**
     * {
     *   "publiclyCallable": "true",
     *   "url": {
     *     "execution-start-time": "0",
     *     "execution-stop-time": "106",
     *     "execution-time": "106",
     *     "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
     *   },
     *   "user-time": "108",
     *   "service-time": "106",
     *   "build-version": "0.2.1997"
     * }
     */
    implicit val diagnosticsUntypedJsonFormat : JsonFormat[UntypedDiagnostics] = jsonFormat(UntypedDiagnostics,
      "publiclyCallable", "url", "user-time", "service-time", "build-version")
    implicit val diagnosticsJsonFormat : JsonFormat[Diagnostics] = isoFormat(diagnosticsUntypedJsonFormat, diagIso)

    /**
     * {
     *   "code": "32",
     *   "date": "9 Nov 2013",
     *   "day": "Sat",
     *   "high": "64",
     *   "low": "46",
     *   "text": "Sunny"
     * }
     */
    implicit val forecastUntypedJsonFormat : JsonFormat[UntypedForecast] = jsonFormat(UntypedForecast,
      "code", "date", "day", "high", "low", "text")
    implicit val forecastJsonFormat : JsonFormat[Forecast] = isoFormat(forecastUntypedJsonFormat, forecastIso)

    /*
     *{
     *  "code": "33",
     *  "date": "Tue, 05 Nov 2013 7:55 PM PST",
     *  "temp": "61",
     *  "text": "Fair"
     * }
     */
    implicit val conditionUntypedJsonFormat : JsonFormat[UntypedCondition] =
      jsonFormat(UntypedCondition, "code", "date", "temp", "text")
    implicit val conditionJsonFormat : JsonFormat[Condition] = isoFormat(conditionUntypedJsonFormat, conditionlIso)

    /**
     * Here lies the magic!
     * @param f
     * @param iso
     * @tparam A
     * @tparam B
     * @return
     */
    def isoFormat[A, B](f: JsonFormat[A], iso: A <=> B) = new RootJsonFormat[B] {
      override def read(json: JsValue): B = iso.to(f.read(json))
      override def write(b: B): JsValue = f.write(iso.from(b))
    }
  }

  def main (args: Array[String]) {
    import MyJsonProtocol._

    val testWind =
      """
        |{
        |  "chill": "61",
        |  "direction": "0",
        |  "speed": "0"
        |}
      """.stripMargin

    val testLocation =
      """
        |{
        |  "city": "Sunnyvale",
        |  "country": "United States",
        |  "region": "CA"
        |}
      """.stripMargin

    val testUnits =
      """
        |{
        |  "distance": "mi",
        |  "pressure": "in",
        |  "speed": "mph",
        |  "temperature": "F"
        |}
      """.stripMargin

    val testUrl =
      """
        |{
        |  "execution-start-time": "0",
        |  "execution-stop-time": "106",
        |  "execution-time": "106",
        |  "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
        |}
      """.stripMargin

    val testForcast =
      """
        |{
        |  "code": "32",
        |  "date": "9 Nov 2013",
        |  "day": "Sat",
        |  "high": "64",
        |  "low": "46",
        |  "text": "Sunny"
        |}
      """
        .stripMargin

    val testGuid =
      """
      |{
      |  "isPermaLink": "false",
      |  "content": "USCA1116_2013_11_09_7_00_PST"
      |}
    """.stripMargin

    val testCondition =
      """
        |{
        |  "code": "33",
        |  "date": "Tue, 05 Nov 2013 7:55 PM PST",
        |  "temp": "61",
        |  "text": "Fair"
        |}
      """.stripMargin

    val testDiagnostics =
      """
        |  {
        |    "publiclyCallable": "true",
        |    "url": {
        |      "execution-start-time": "0",
        |      "execution-stop-time": "106",
        |      "execution-time": "106",
        |      "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
        |    },
        |    "user-time": "108",
        |    "service-time": "106",
        |    "build-version": "0.2.1997"
        |  }
      """.stripMargin

    val testForecastList =
      """
        |[
        |            {
        |              "code": "33",
        |              "date": "5 Nov 2013",
        |              "day": "Tue",
        |              "high": "72",
        |              "low": "48",
        |              "text": "Mostly Clear"
        |            },
        |            {
        |              "code": "30",
        |              "date": "6 Nov 2013",
        |              "day": "Wed",
        |              "high": "75",
        |              "low": "51",
        |              "text": "Partly Cloudy"
        |            },
        |            {
        |              "code": "30",
        |              "date": "7 Nov 2013",
        |              "day": "Thu",
        |              "high": "68",
        |              "low": "49",
        |              "text": "AM Clouds/PM Sun"
        |            },
        |            {
        |              "code": "30",
        |              "date": "8 Nov 2013",
        |              "day": "Fri",
        |              "high": "66",
        |              "low": "45",
        |              "text": "Partly Cloudy"
        |            },
        |            {
        |              "code": "32",
        |              "date": "9 Nov 2013",
        |              "day": "Sat",
        |              "high": "64",
        |              "low": "46",
        |              "text": "Sunny"
        |            }
        |          ]
      """.stripMargin

    val megaTest =
      """
        |{
        |  "query": {
        |    "count": 1,
        |    "created": "2013-11-06T04:18:34Z",
        |    "lang": "en-US",
        |    "diagnostics": {
        |      "publiclyCallable": "true",
        |      "url": {
        |        "execution-start-time": "0",
        |        "execution-stop-time": "106",
        |        "execution-time": "106",
        |        "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
        |      },
        |      "user-time": "108",
        |      "service-time": "106",
        |      "build-version": "0.2.1997"
        |    },
        |    "results": {
        |      "channel": {
        |        "title": "Yahoo! Weather - Sunnyvale, CA",
        |        "link": "http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html",
        |        "description": "Yahoo! Weather for Sunnyvale, CA",
        |        "language": "en-us",
        |        "lastBuildDate": "Tue, 05 Nov 2013 7:55 pm PST",
        |        "ttl": "60",
        |        "location": {
        |          "city": "Sunnyvale",
        |          "country": "United States",
        |          "region": "CA"
        |        },
        |        "units": {
        |          "distance": "mi",
        |          "pressure": "in",
        |          "speed": "mph",
        |          "temperature": "F"
        |        },
        |        "wind": {
        |          "chill": "61",
        |          "direction": "0",
        |          "speed": "0"
        |        },
        |        "atmosphere": {
        |          "humidity": "52",
        |          "pressure": "30.19",
        |          "rising": "1",
        |          "visibility": "10"
        |        },
        |        "astronomy": {
        |          "sunrise": "6:37 am",
        |          "sunset": "5:06 pm"
        |        },
        |        "image": {
        |          "title": "Yahoo! Weather",
        |          "width": "142",
        |          "height": "18",
        |          "link": "http://weather.yahoo.com",
        |          "url": "http://l.yimg.com/a/i/brand/purplelogo//uh/us/news-wea.gif"
        |        },
        |        "item": {
        |          "title": "Conditions for Sunnyvale, CA at 7:55 pm PST",
        |          "lat": "37.37",
        |          "long": "-122.04",
        |          "link": "http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html",
        |          "pubDate": "Tue, 05 Nov 2013 7:55 pm PST",
        |          "condition": {
        |            "code": "33",
        |            "date": "Tue, 05 Nov 2013 7:55 pm PST",
        |            "temp": "61",
        |            "text": "Fair"
        |          },
        |          "description": "\n<img src=\"http://l.yimg.com/a/i/us/we/52/33.gif\"/><br />\n<b>Current Conditions:</b><br />\nFair, 61 F<BR />\n<BR /><b>Forecast:</b><BR />\nTue - Mostly Clear. High: 72 Low: 48<br />\nWed - Partly Cloudy. High: 75 Low: 51<br />\nThu - AM Clouds/PM Sun. High: 68 Low: 49<br />\nFri - Partly Cloudy. High: 66 Low: 45<br />\nSat - Sunny. High: 64 Low: 46<br />\n<br />\n<a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/Sunnyvale__CA/*http://weather.yahoo.com/forecast/USCA1116_f.html\">Full Forecast at Yahoo! Weather</a><BR/><BR/>\n(provided by <a href=\"http://www.weather.com\" >The Weather Channel</a>)<br/>\n",
        |          "forecast": [
        |            {
        |              "code": "33",
        |              "date": "5 Nov 2013",
        |              "day": "Tue",
        |              "high": "72",
        |              "low": "48",
        |              "text": "Mostly Clear"
        |            },
        |            {
        |              "code": "30",
        |              "date": "6 Nov 2013",
        |              "day": "Wed",
        |              "high": "75",
        |              "low": "51",
        |              "text": "Partly Cloudy"
        |            },
        |            {
        |              "code": "30",
        |              "date": "7 Nov 2013",
        |              "day": "Thu",
        |              "high": "68",
        |              "low": "49",
        |              "text": "AM Clouds/PM Sun"
        |            },
        |            {
        |              "code": "30",
        |              "date": "8 Nov 2013",
        |              "day": "Fri",
        |              "high": "66",
        |              "low": "45",
        |              "text": "Partly Cloudy"
        |            },
        |            {
        |              "code": "32",
        |              "date": "9 Nov 2013",
        |              "day": "Sat",
        |              "high": "64",
        |              "low": "46",
        |              "text": "Sunny"
        |            }
        |          ],
        |          "guid": {
        |            "isPermaLink": "false",
        |            "content": "USCA1116_2013_11_09_7_00_PST"
        |          }
        |        }
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    println(List(
     // roundTripEq(""""9 Nov 2013"""")(dateTimeFormat("dd MMM yyyy"))
     //,roundTripEq(""""Tue, 05 Nov 2013 7:55 PM PST"""")(dateTimeFormat("EEE, dd MMM yyyy h:mm aa zzz"))
       roundTripEq[Url](testUrl)
      ,roundTripEq[Diagnostics](testDiagnostics)
      ,roundTripEq[Forecast](testForcast)
      ,roundTripEq[List[Forecast]](testForecastList)
      ,roundTripEq[Condition](testCondition)
      ,roundTripEq[Guid](testGuid)
      ,roundTripEq[Units](testUnits)
      ,roundTripEq[Location](testLocation)
      ,roundTripEq[Wind](testWind)
    ).forall(identity))
  }

  def roundTripEq[A: JsonFormat](source:String): Boolean = {
    val jsonAst  = source.parseJson
    val fromJson = jsonAst.convertTo[A]
    val toJson   = fromJson.toJson
    if (jsonAst != toJson) {
      println(fromJson)
      println(jsonAst)
      println(toJson)
    }
    jsonAst == toJson
  }

  def unmarshall[A](s:String)(implicit f: JsonFormat[A]): A = s.parseJson.convertTo[A]
}


//
//    implicit class RichJsValueMap(m: Map[String, JsValue]) {
//      def getString(key: String): String = m(key).asString
//    }
//
//    implicit class RichJsValue(jv: JsValue) {
//      def asString: String = jv match { case JsString(s) => s }
//    }
//
//

//
//    def JsonIntString   (i:Int)     : JsString = JsString(i.toString)
//    def JsonLongString  (i:Long)    : JsString = JsString(i.toString)
//    def JsonDoubleString(i:Double)  : JsString = JsString(i.toString)
//    def JsonBoolString  (i:Boolean) : JsString = JsString(i.toString)
//
//    def stringReader[A: ru.TypeTag](value:JsValue)(f: Seq[String] => A): A = {
//      val fieldNames = ru.typeOf[A].members.collect {
//        case m: ru.MethodSymbol if m.isGetter => m
//      }.map(_.name.toString).toSeq.sorted
//      val stringValues = value.asJsObject.getFields(fieldNames:_*).map { case JsString(s) => s }
//      f(stringValues)
//    }
//
//    def getFields(a: AnyRef): List[(String, Any)] =
//      (List[(String, Any)]() /: a.getClass.getDeclaredFields) { (l, f) =>
//        f.setAccessible(true)
//        (f.getName -> f.get(a) :: l ).reverse
//      }


//  trait Wrapped[A]{ val a: A }
//  case class Wrapper[A](a:A) extends Wrapped[A]
//  implicit def wrapBool(b:Boolean): Wrapped[Boolean] = Wrapper(b)
//  implicit def unwrap[A](a: A)(implicit w: Wrapped[A]): A = w.a
//    implicit def wrappedFormat[A: Wrapped](implicit f: JsonFormat[A]) = new RootJsonFormat[Wrapped[A]]{
//      def write(w: Wrapped[A]): JsValue = JsString(s""""${f.write(w.a)}"""")
//      def read(json: JsValue): Wrapped[A] = json match {
//        case JsString(s) => try Wrapper(f.read(s.parseJson))
//        catch { case t: Throwable => sys.error(s) }
//        case _ => error(json.toString())
//      }
//    }

//    def writer[A](a: AnyRef, m: Map[String, Any => JsValue]): JsValue =
//      JsObject(getFields(a).foldLeft(Map[String, JsValue]()){
//        case (acc, (name, value)) => acc + (name -> m(name)(value))
//      })

//println(new Guid(false, "dood").productIterator.toList)
//println(getFields(unmarshall[Guid](testGuid)))
//println(getFields(unmarshall[Forecast](testForcast)))


//    implicit object UntypedUrlJsonFormat extends RootJsonFormat[UntypedUrl] {
//      def write(u: UntypedUrl) = JsObject(
//        "execution-start-time" -> JsString(u.executionStartTime),
//        "execution-stop-time"  -> JsString(u.executionEndTime),
//        "execution-time"       -> JsString(u.executionTime),
//        "content"              -> JsString(u.content)
//      )
//
//      def read(value: JsValue) = value match {
//        case JsObject(m) => new UntypedUrl(
//          m("execution-start-time").asString,
//          m("execution-stop-time").asString,
//          m("execution-time").asString,
//          m("content").asString
//        )
//        case _ => deserializationError("Color expected")
//      }
//    }

//    implicit object ConditionFormat extends RootJsonFormat[Condition] {
//      val format = dateTimeFormat("EEE, dd MMM yyyy h:mm aa zzz")
//      def write(f: Condition) = JsObject(
//        "code" -> JsonIntString(f.code),
//        "date" -> format.write(f.date),
//        "temp" -> JsonIntString(f.temp),
//        "text" -> JsString(f.text)
//      )
//      def read(value: JsValue): Condition = stringReader(value){
//        case Seq(code, date, temp, text) =>
//          new Condition(code.toInt, format.readString(date), temp.toInt, text)
//        case _ => sys.error(s"Bad Forecast, : $value")
//      }
//    }

///* Examples:
// * "EEE, dd MMM yyyy H:mm aa zzz"
// * "dd MMM yyyy"
// *
// * Info at:
// *   https://gist.github.com/chronodm/7684755
// *   http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
// */
//def dateTimeFormat(formatString: String) = new RootJsonFormat[DateTime] {
//  val formatter = DateTimeFormat.forPattern(formatString)
//  def readString(s: String) : DateTime = formatter.parseDateTime(s)
//  def write(obj: DateTime): JsValue = {
//    val out = formatter.print(obj)
//    // TODO: this is bad, it was only for the original format!...
//    JsString(if(out.startsWith("0")) out.drop(1) else out)
//  }
//  def read(value: JsValue): DateTime = value match {
//    case JsString(s) => readString(s)
//    case bad => throw new DeserializationException("invalid DateTime: " + bad)
//  }
//}
