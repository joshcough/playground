import scalaz._, Scalaz._
import argonaut._, Argonaut._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import ExampleJson._

object ArgonautTest {

  def dateDecoder(pattern: String): DecodeJson[DateTime] =
    optionDecoder(x => x.string.flatMap(s =>
      tryTo(DateTimeFormat.forPattern(pattern).parseDateTime(s))), "DateTime")

  // {"CadetBlue": {"red":95,"green":158,"blue":160}}
  case class RGB(red: Int, green: Int, blue: Int)
  case class Color(name: String, rgb: RGB)
  implicit def RGBDecodeJSon: CodecJson[RGB] =
    casecodec3(RGB.apply, RGB.unapply)("red", "green", "blue")
  implicit def ColorDecodeJson: DecodeJson[Color] = {
    val msr: DecodeJson[Map[String, RGB]] = MapDecodeJson
    msr.map{ case m: Map[String, RGB] => m.toList.head match { case (k, rgb) => Color(k, rgb) } }
  }

  implicit def TopDecodeJson: DecodeJson[Top] = jdecode1L(Top.apply)("query")

  // created": "2013-11-06T04:18:34Z"  // what the heck is the T?
  //val queryDateDecoder: DecodeJson[DateTime] = dateDecoder("yyyy-dd-MMh:mm aa")
  implicit def QueryDecodeJson: DecodeJson[Query] = {
    //implicit val f = queryDateDecoder
    jdecode5L(Query.apply)("count", "created", "lang", "diagnostics", "results")
  }

  implicit def ResultsDecodeJson: DecodeJson[Results] = jdecode1L(Results.apply)("channel")

  //{"sunrise": "6:37 am", "sunset": "5:06 pm"}
  val astronomyDateDecoder: DecodeJson[DateTime] = dateDecoder("h:mm aa")
  implicit def AstronomyDecodeJson: DecodeJson[Astronomy] = {
    implicit val f = astronomyDateDecoder
    jdecode2L(Astronomy.apply)("sunrise", "sunset")
  }
  //"pubDate": "Tue, 05 Nov 2013 7:55 pm PST",
  val itemDateDecoder: DecodeJson[DateTime] = dateDecoder("EEE, dd MMM yyyy h:mm aa zzz")
  implicit def ItemDecodeJson: DecodeJson[Item] = {
    implicit val f = itemDateDecoder
    jdecode9L(Item.apply)("title", "lat", "long", "link", "pubDate", "condition", "description", "forecast", "guid")
  }

  val channelDateDecoder: DecodeJson[DateTime] = dateDecoder("EEE, dd MMM yyyy h:mm aa zzz")
  implicit def ChannelDecodeJson: DecodeJson[Channel] = {
    implicit val c = channelDateDecoder
    jdecode13L(Channel.apply)(
      "title", "link", "description", "language", "lastBuildDate", "ttl", "location",
      "units", "wind", "atmosphere", "astronomy", "image", "item")
  }

  implicit def ImageCodecJson: CodecJson[Image] =
    casecodec5(Image.apply, Image.unapply)("title", "width", "height", "link", "url")
  implicit def DiagnosticsCodecJson: CodecJson[Diagnostics] =
    casecodec5(Diagnostics.apply, Diagnostics.unapply)("publiclyCallable", "url", "user-time", "service-time", "build-version")
  implicit def UrlCodecJson: CodecJson[Url] =
    casecodec4(Url.apply, Url.unapply)("execution-start-time", "execution-stop-time", "execution-time", "content")
  implicit def LocationCodecJson: CodecJson[Location] =
    casecodec3(Location.apply, Location.unapply)("city", "country", "region")
  implicit def UnitsCodecJson: CodecJson[Units] =
    casecodec4(Units.apply, Units.unapply)("distance", "pressure", "speed", "temperature")
  implicit def WindCodecJson: CodecJson[Wind] =
    casecodec3(Wind.apply, Wind.unapply)("chill", "direction", "speed")
  implicit def AtmosphereCodecJson: CodecJson[Atmosphere] =
    casecodec4(Atmosphere.apply, Atmosphere.unapply)("humidity", "pressure", "rising", "visibility")
  implicit def GuidCodecJson: CodecJson[Guid] =
    casecodec2(Guid.apply, Guid.unapply)("isPermaLink", "content")
  // just decoding here...
  val forecastDateDecoder: DecodeJson[DateTime] = dateDecoder("d MMM yyyy") ||| dateDecoder("dd MMM yyyy")
  implicit def ForecastDecodeJson: DecodeJson[Forecast] = {
    implicit val f = forecastDateDecoder
    jdecode6L(Forecast.apply)("code", "date", "day", "high", "low", "text")
  }
  val conditionDateDecoder: DecodeJson[DateTime] = dateDecoder("EEE, dd MMM yyyy h:mm aa zzz")
  implicit def ConditionDecodeJson: DecodeJson[Condition] = {
    implicit val f = conditionDateDecoder
    jdecode4L(Condition.apply)("code", "date", "temp", "text")
  }

  def main (args: Array[String]): Unit = {
    List(
       test[Color]("""{"CadetBlue": {"red":95,"green":158,"blue":160}}""")
      ,test[Guid](testGuid)
      ,test[Forecast](testForcast)
      ,test[DateTime]("\"9 Nov 2013\"")(forecastDateDecoder)
      //,test[DateTime]("\"9 Goo 2013\"")(forecastDateDecoder)
      ,test[DateTime]("\"Tue, 05 Nov 2013 7:55 PM PST\"")(conditionDateDecoder)
      ,test[Condition](testCondition)
      ,test[Url](testUrl)
      ,test[Diagnostics](testDiagnostics)
      ,test[Astronomy](testAstronomy)
      ,test[Image](testImage)
      ,test[Item](testItem)
      ,test[Channel](testChannel)
      ,test[Results](testResults)
      ,test[Query](testQuery)
      ,test[Top](megaTest)
    ).foreach(println)
  }

  def test[A: DecodeJson](s: String) = Parse.decodeEither[A](s)
}
