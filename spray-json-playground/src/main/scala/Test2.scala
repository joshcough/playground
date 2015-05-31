import spray.json._
//import DefaultJsonProtocol._
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter, DateTimeFormat}
import scala.reflect.runtime.{currentMirror => m, universe => ru}
import scalaz._
import Scalaz._
import Isomorphism.<=>

/**
 * Created by joshcough on 5/31/15.
 */
object Test2 {

  case class Guid(isPermaLink: Boolean, content: String)
  case class Forecast (code: Int, date: DateTime, day: String, high: Int, low: Int, text: String)
  case class Condition(code: Int, date: DateTime, temp: Int, text: String)

  /*
  {
    "execution-start-time": "0",
    "execution-stop-time": "106",
    "execution-time": "106",
    "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
  }
   */
  case class UrlUntyped(executionStartTime: String, executionEndTime: String, executionTime: String, content: String)
  case class Url(executionStartTime: Int, executionEndTime: Int, executionTime: Int, content: String)

  implicit val isoFamilyRelic = new (UrlUntyped <=> Url) {
    val to: UrlUntyped => Url = u =>
      Url(u.executionStartTime.toInt, u.executionEndTime.toInt, u.executionTime.toInt, u.content)
    val from: Url => UrlUntyped = u =>
      UrlUntyped(u.executionStartTime.toString, u.executionEndTime.toString, u.executionTime.toString, u.content)
  }

  /*
  {
    "publiclyCallable": "true",
    "url": {
      "execution-start-time": "0",
      "execution-stop-time": "106",
      "execution-time": "106",
      "content": "http://weather.yahooapis.com/forecastrss?w=2502265"
    },
    "user-time": "108",
    "service-time": "106",
    "build-version": "0.2.1997"
  }
   */
  case class Diagnostics(publiclyCallable: Boolean, url: Url, userTime: Int, serviceTime: Int, buildVersion: String)


  object MyJsonProtocol extends DefaultJsonProtocol {

    implicit val urlUntypedFormat = jsonFormat4(UrlUntyped)

    implicit object guidFormat extends RootJsonFormat[Guid] {
      def write(g: Guid) = JsObject(
        "isPermaLink" -> JsonBoolString(g.isPermaLink),
        "content" -> JsString(g.content)
      )
      def read(value: JsValue) =
        value.asJsObject.getFields("isPermaLink", "content") match {
          case Seq(JsString(isPermaLink), JsString(content)) => new Guid(isPermaLink.toBoolean, content)
          case bad => throw new DeserializationException("Guid expected: " + bad)
        }
    }

    /* Examples:
     * "EEE, dd MMM yyyy H:mm aa zzz"
     * "dd MMM yyyy"
     *
     * Info at:
     *   https://gist.github.com/chronodm/7684755
     *   http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
     */
    def dateTimeFormat(formatString: String) = new RootJsonFormat[DateTime] {
      val formatter = DateTimeFormat.forPattern(formatString)
      def readString(s: String) : DateTime = formatter.parseDateTime(s)
      def write(obj: DateTime): JsValue = {
        val out = formatter.print(obj)
        JsString(if(out.startsWith("0")) out.drop(1) else out)
      }
      def read(value: JsValue): DateTime = value match {
        case JsString(s) => readString(s)
        case bad => throw new DeserializationException("invalid DateTime: " + bad)
      }
    }

    def JsonIntString   (i:Int)     : JsString = JsString(i.toString)
    def JsonLongString  (i:Long)    : JsString = JsString(i.toString)
    def JsonDoubleString(i:Double)  : JsString = JsString(i.toString)
    def JsonBoolString  (i:Boolean) : JsString = JsString(i.toString)

    implicit object ForecastFormat extends RootJsonFormat[Forecast] {
      val format = dateTimeFormat("dd MMM yyyy")
      def write(f: Forecast) = JsObject(
        "code" -> JsonIntString(f.code),
        "date" -> format.write(f.date),
        "day"  -> JsString(f.day),
        "high" -> JsonIntString(f.high),
        "low"  -> JsonIntString(f.low),
        "text" -> JsString(f.text)
      )
      def read(value: JsValue): Forecast = stringReader(value){
        case Seq(code, date, day, high, low, text) =>
          new Forecast(code.toInt, format.readString(date), day, high.toInt, low.toInt, text)
        case _ => sys.error(s"Bad Forecast, : $value")
      }
    }

    //  case class Condition(code: Int, date: DateTime, temp: Int, text: String)
    implicit object ConditionFormat extends RootJsonFormat[Condition] {
      val format = dateTimeFormat("EEE, dd MMM yyyy h:mm aa zzz")
      def write(f: Condition) = JsObject(
        "code" -> JsonIntString(f.code),
        "date" -> format.write(f.date),
        "temp" -> JsonIntString(f.temp),
        "text" -> JsString(f.text)
      )
      def read(value: JsValue): Condition = stringReader(value){
        case Seq(code, date, temp, text) =>
          new Condition(code.toInt, format.readString(date), temp.toInt, text)
        case _ => sys.error(s"Bad Forecast, : $value")
      }
    }

    def stringReader[A: ru.TypeTag](value:JsValue)(f: Seq[String] => A): A = {
      val fieldNames = ru.typeOf[A].members.collect {
        case m: ru.MethodSymbol if m.isGetter => m
      }.map(_.name.toString).toSeq.sorted
      val stringValues = value.asJsObject.getFields(fieldNames:_*).map { case JsString(s) => s }
      f(stringValues)
    }

    def getFields(a: AnyRef): List[(String, Any)] =
      (List[(String, Any)]() /: a.getClass.getDeclaredFields) { (l, f) =>
        f.setAccessible(true)
        (f.getName -> f.get(a) :: l ).reverse
      }

  }

  def main (args: Array[String]) {
    import MyJsonProtocol._

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
      |"isPermaLink": "false",
      |"content": "USCA1116_2013_11_09_7_00_PST"
      |}
    """.stripMargin

    val testCondition =
      """
        |{
        |            "code": "33",
        |            "date": "Tue, 05 Nov 2013 7:55 PM PST",
        |            "temp": "61",
        |            "text": "Fair"
        |          }
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
      roundTripEq(""""9 Nov 2013"""")(dateTimeFormat("dd MMM yyyy")),
      roundTripEq(""""Tue, 05 Nov 2013 7:55 PM PST"""")(dateTimeFormat("EEE, dd MMM yyyy h:mm aa zzz")),
      roundTripEq[Forecast](testForcast),
      roundTripEq[List[Forecast]](testForecastList),
      roundTripEq[Condition](testCondition),
      roundTripEq[Guid](testGuid)
    ).forall(identity))

    case class Booger(i: Int, s: String)
    val j: JsonFormat[Booger] = jsonFormat(Booger, "i", "s")
    println(j.read("""{ "i": 7, "s": "hi" }""".parseJson))
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