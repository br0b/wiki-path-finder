import sttp.client4.*
import sttp.client4.circe.*
import io.circe.*
import io.circe.parser.*
import io.circe.Decoder
import io.circe.Decoder.*
import io.circe.generic.auto.*
import Problem.*
import com.google.common.util.concurrent.RateLimiter

import scala.util.Try

type Article = String

trait WikiAPIInterface:
  def linkedArticles(article: String, language: String, limiter: RateLimiter): Set[Article]

object WikiApi extends WikiAPIInterface :
  override def linkedArticles(article: String, language: String, limiter: RateLimiter): Set[Article] =
   val request = basicRequest
      .get(uri"https://$language.wikipedia.org/w/api.php?action=parse&page=$article&prop=links&format=json")
      .response(asString)

    val backend = DefaultSyncBackend()

    limiter.acquire(1)

    request.send(backend).body match {
      case Right(json: String) =>
        for link <- getAllLinksFromJson(json).filter(isLinkLegal)
                                             .filter(checkIfArticleExists) yield link.article
      case Left(responseCode: String) =>
        println(s"Response code: $responseCode")
        Set()
    }

def isLinkLegal(link: Link): Boolean =
  link.exists match
    case Some(string) => link.ns == 0
    case None => false

def getAllLinksFromJson(jsonRaw: String): Set[Link] =
  val jsonRawWithReplacedAsterixes = jsonRaw.replaceAll("\\*", "article")
  parse(jsonRawWithReplacedAsterixes) match {
    case Right(json) =>
      val hcursora = json.hcursor
      val hcursorb = hcursora.downField("parse")
      hcursorb.get[Set[Link]]("links") match
        case Right(links: Set[Link]) => links
        case Left(decodingFailure: DecodingFailure) =>
          println(decodingFailure.getMessage)
          Set()
    case Left(errorMessage) =>
      println(errorMessage)
      Set()
  }

def checkIfArticleExists(link: Link): Boolean =
  link.exists match
    case Some(string) => true
    case None => false