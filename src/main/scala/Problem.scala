import cats.syntax.all._
import upperbound._
import cats.effect._
import scala.concurrent.duration._
import sttp.client4

case class Problem(
  language: String,
  start: String,
  end: String
)

trait ProblemInterface:
  def solve(problem: Problem): Either[String, List[String]]
  def getProblemFromString(string: String): Problem

object Problem extends ProblemInterface:
  override def getProblemFromString(string: String): Problem =
    val tuple = string.substring(1, string.length() - 1).split(",").toVector.map(_.trim)
    Problem(tuple(0), tuple(1), tuple(2))

  override def solve(problem: Problem): Either[String, List[String]] =
    val MAX_DEPTH = 6
    val path = List(problem.start)

    val limiter = Limiter.start[IO](150 every 1.second) // limit of 200 requests per second

    _solve(path, problem, MAX_DEPTH, limiter) match
      case Some(finalPath) => Right(finalPath)
      case None => Left("No paths found.")

def _solve(path: List[String], problem: Problem, MAX_DEPTH: Int, limiter: Limiter[IO]): Option[List[String]] =
  if isPathFinal(path, problem) then return Some(path)
  if path.length == MAX_DEPTH then return None

  val linkedArticles = limiter.submit(getLinkedArticles(path.head, problem.language))

  for article <- linkedArticles do _solve(article :: path, problem, MAX_DEPTH) match {
    case Some(finalPath) => return Some(finalPath)
    case None =>
  }

  None

def getLinkedArticles(article: String, language: String): Seq[String] =
  val url = s"https://$language.wikipedia.org/w/api.php?action=parse&page=$article&format=json&prop=links"


def isPathFinal(path: List[String], problem: Problem): Boolean =
  path.head == problem.end