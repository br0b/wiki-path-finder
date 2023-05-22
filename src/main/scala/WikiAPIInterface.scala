import io.circe.Decoder, io.circe.generic.auto._
import sttp.client4.*
import sttp.client4.circe.*

import Link._

trait WikiAPIInterface:
  def getLinksToArticles(article: String, language: String): Option[Seq[Link]]


object WikiApi extends WikiAPIInterface :
  override def getLinksToArticles(article: String, language: String): Option[List[Link]] =
   val request = basicRequest
      .get(uri"https://$language.wikipedia.org/w/api.php?action=parse&page=$article&format=json&prop=links")
      .response(asString)

    val backend = DefaultSyncBackend()
    request.send(backend).body match {
      case Right(json: String) =>
        getAllLinksFromJson(json) match {
          case Right(allLinks: List[Link]) => Some(allLinks.filter(isLinkToArticle))
          case Left(error) =>
            println(error.getMessage)
            None
        }
      case Left(responseCode: String) =>
        println(s"Response code: $responseCode")
        None
    }

def getAllLinksFromJson(json: String): Either[io.circe.Error, List[Link]] =
  val decodeLinks = Decoder[List[Link]].prepare(_.downField("links"))
  io.circe.parser.decode(json)(decodeLinks)