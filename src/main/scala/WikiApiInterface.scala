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

/**
 *  A wikipedia article. In this project, article's title is the article.
 */
type Article = String

/**
 * A link that has the same fields as a link specified in a json given by Wikipedia's API.
 *
 * @param ns namespace of the article this link points to
 * @param exists an empty String if the article this link points to exists, None otherwise
 * @param article the article this link points to
 */
case class Link(
  ns: Int,
  exists: Option[String],
  article: Article
)

trait WikiAPIInterface:
  /**
   * Get all articles that can belong to solution of a problem specified in this project's specification.
   *
   * @param article  the article from which we the links have to go out
   * @param language the language of all of the articles
   * @param limiter  a limiter used for rate limiting get requests
   * @return a set of articles connected directly to the "article" article
   */
  def linkedArticles(article: String, language: String, limiter: RateLimiter): Set[Article]

object WikiApi extends WikiAPIInterface:
  override def linkedArticles(article: String, language: String, limiter: RateLimiter): Set[Article] =
    // A request conforming to the Wikipedia API specification. It retrieves links in a JSON format.
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

/**
 * Check if we a solution can contain this link. It has to connect to wikipedia articles and the article it points to
 * has to exist.
 *
 * @param link the link whose legality we check
 * @return true if the link is legal, false otherwise
 */
def isLinkLegal(link: Link): Boolean =
  link.exists match
    case Some(string) => link.ns == 0
    case None => false

/**
 * Get all links from a json retrieved using Wikipedia's API.
 *
 * @param jsonRaw a raw json
 * @return a set of all links outgoing from the wikipedia article represented by jsonRaw
 */
def getAllLinksFromJson(jsonRaw: String): Set[Link] =
  val jsonRawWithReplacedAsterixes = jsonRaw.replaceAll("\\*", "article")
  parse(jsonRawWithReplacedAsterixes) match {
    case Right(json) =>
      json.hcursor.downField("parse").get[Set[Link]]("links") match
        case Right(links: Set[Link]) => links
        case Left(decodingFailure: DecodingFailure) =>
          println(decodingFailure.getMessage)
          Set()
    case Left(errorMessage) =>
      println(errorMessage)
      Set()
  }

/**
 * Check if the article the link points to exists.
 *
 * @param link pointing to article we are checking
 * @return true if the article exists, false otherwise
 */
def checkIfArticleExists(link: Link): Boolean =
  link.exists match
    case Some(string) => true
    case None => false