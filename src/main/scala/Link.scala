case class Link(
  namespace: Int,
  exists: String,
  title: String
)

val MainWikipediaNamespace = 0

trait LinkInterface:
  def isLinkToArticle(link: Link): Boolean

object Link extends LinkInterface:
  def isLinkToArticle(link: Link): Boolean =
    link.namespace == MainWikipediaNamespace