import WikiApi.getLinksToArticles
import com.google.common.util.concurrent.RateLimiter
import sttp.client4.*

import Path._

type Article = String

case class Problem (
  language: String,
  start: String,
  end: String
)


trait ProblemInterface:
  def solve(problem: Problem, maxPathLength: Int): Option[List[Path]]
  def getProblemFromString(string: String): Problem

object Problem extends ProblemInterface:
  override def getProblemFromString(string: String): Problem =
    val tuple = string.substring(1, string.length() - 1).split(",").toVector.map(_.trim)
    Problem(tuple(0), tuple(1), tuple(2))

  override def solve(problem: Problem, maxPathLength: Int): Option[List[Path]] =
    val limiter = RateLimiter.create(150) // limit of 150 requests per second

    _solve(Path(problem.start), problem, maxPathLength, limiter) match {
      case Some(paths) => Some(paths)
      case None => None
    }

/**
 * @param currentPath
 * @param problem
 * @param MAX_DEPTH
 * @param limiter
 * @return Returns list of all paths that are final and such that currentPath is their prefix.
 *         If no paths exist, returns None.
 */
def _solve(currentPath: Path, problem: Problem,
           maxPathLength: Int, limiter: RateLimiter): Option[List[Path]] =
  if isPathFinal(currentPath, problem) then return Some(List(currentPath))
  if currentPath.length == maxPathLength then return None

  val currentArticle = currentPath.head

  limiter.acquire()

  val paths = getLinksToArticles(currentArticle, problem.language) match {
    case Some(links) =>
      for {
        link <- links
      } yield {
        _solve(link.title :: currentPath, problem, maxPathLength, limiter) match
          case Some(paths) => paths
          case None => List()
      }.flatten
    case None => List()
  }

  if paths.nonEmpty then Some(paths) else None

def isPathFinal(path: Path, problem: Problem): Boolean =
  path.head == problem.end