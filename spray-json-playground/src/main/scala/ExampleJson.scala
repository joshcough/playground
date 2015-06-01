import org.joda.time.DateTime

/**
 * Created by joshcough on 5/31/15.
 */
object ExampleJson {

  case class Location(city: String, country: String, region: String)
  case class Units(distance: String, pressure: String, speed: String, temperature: String)
  case class Wind(chill: Int, direction: Int, speed: Int)
  case class Atmosphere(humidity: Int, pressure: Double, rising: Int, visibility: Int)
  case class Guid(isPermaLink: Boolean, content: String)
  case class Forecast (code: Int, date: DateTime, day: String, high: Int, low: Int, text: String)
  case class Condition(code: Int, date: DateTime, temp: Int, text: String)
  case class Url(executionStartTime: Int, executionEndTime: Int, executionTime: Int, content: String)
  case class Diagnostics(publiclyCallable: Boolean, url: Url, userTime: Int, serviceTime: Int, buildVersion: String)

  val testAtmosphere =
    """
      |{
      |  "humidity": "52",
      |  "pressure": "30.19",
      |  "rising": "1",
      |  "visibility": "10"
      |}
    """.stripMargin

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

}
