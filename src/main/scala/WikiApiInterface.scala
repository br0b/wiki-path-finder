import sttp.client4.*
import sttp.client4.circe.*
import io.circe.*
import io.circe.parser.*
import io.circe.Decoder
import io.circe.Decoder._
import io.circe.generic.auto._
import Problem.*

import scala.util.Try

type Article = String

trait WikiAPIInterface:
  def linkedArticles(article: String, language: String): Seq[Article]

object WikiApi extends WikiAPIInterface :
  override def linkedArticles(article: String, language: String): Seq[Article] =
   val request = basicRequest
      .get(uri"https://$language.wikipedia.org/w/api.php?action=parse&page=$article&prop=links&format=json")
      .response(asString)

    val backend = DefaultSyncBackend()
    request.send(backend).body match {
      case Right(json: String) => getAllLinksFromJson(json) match {
        case Some(links) =>
          for link <- links.filter(isLinkLegal) yield link.article
        case None => None
      }
      case Left(responseCode: String) =>
        println(s"Response code: $responseCode")
        None
    }

def isLinkLegal(link: Link): Boolean =
  link.exists match
    case Some(string) => link.ns == 0
    case None => false

def getAllLinksFromJson(jsonRaw: String): Seq[Link] =
  val jsonRawWithReplacedAsterixes = jsonRaw.replaceAll("\\*", "article")
  parse(jsonRawWithReplacedAsterixes) match {
    case Right(json) =>
      val hcursora = json.hcursor
      val hcursorb = hcursora.downField("parse")
      hcursorb.get[Seq[Link]]("links") match
        case Right(links: Seq[Link]) => Some(links)
        case Left(decodingFailure: DecodingFailure) =>
          println(decodingFailure.getMessage)
          None
    case Left(errorMessage) => {
      println(errorMessage)
      None
    }
  }

def checkIfArticleExists(hCursor: HCursor): Boolean =
  hCursor.keys match
    case Some(keys) => keys.size == 3
    case None => false