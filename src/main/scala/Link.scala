import WikiApi.*

case class Link(
  ns: Int,
  exists: Option[String],
  article: Article
)