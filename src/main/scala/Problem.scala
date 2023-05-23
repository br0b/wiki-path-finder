import com.google.common.util.concurrent.RateLimiter
import sttp.client4.*

import Path._
import WikiApi._

case class Problem (
  language: String,
  start: String,
  end: String
)

trait ProblemInterface:
  def solve(problem: Problem, maxPathLength: Int): Option[Seq[Path]]
  def getProblemFromString(string: String): Problem

object Problem extends ProblemInterface:
  override def getProblemFromString(string: String): Problem =
    val tuple = string.substring(1, string.length() - 1).split(",").toVector.map(_.trim)
    Problem(tuple(0), tuple(1), tuple(2))

  override def solve(problem: Problem, maxPathLength: Int): Set[Path] =
    def loop(
      pathsFound: Set[Path],
      pathsToExplore: Set[Path],
      problem: Problem,
      maxPathLength: Int,
      visitedArticles: Set[Article],
      limiter: RateLimiter): Set[Path] = {
      if (pathsToExplore.isEmpty) return pathsFound

      val newPaths = {
        for {
          path <- pathsToExplore
        } yield linkedArticles(path.head, problem.language) +: path
      }.flatten.diff(visitedArticles)

      loop(
        pathsFound, pathsToExplore, problem, maxPathLength, visitedArticles, limiter
      )
    }

    val limiter = RateLimiter.create(150) // limit of 150 requests per second
    val pathsToExplore = Set[Path](Path(problem.start))
    val visited = Set[Article](problem.start)

    val paths = loop(Set(), pathsToExplore, problem, maxPathLength, visited, limiter)

    if paths.nonEmpty
    then
      Some(paths.map(reverse))
    else
      None

/*
/**
 * @param currentPath
 * @param problem
 * @param MAX_DEPTH
 * @param limiter
 * @return Returns list of all paths that are final and such that currentPath is their prefix.
 *         If no paths exist, returns None.
 */
def _solve(currentPath: Path, problem: Problem,
           maxPathLength: Int, limiter: RateLimiter): Option[Seq[Path]] =
  println(pathToString(currentPath))
  if isPathFinal(currentPath, problem) then return Some(List(currentPath))
  if currentPath.length == maxPathLength then return None

  limiter.acquire()

  val paths = getLinkedArticles(currentArticle, problem.language) match {
    case Some(articles) => {
      def loop(currentPath: Path, problem: Problem, maxPathLength)
      for {
        article <- articles
      } yield {
        _solve(currentPath :+ article, problem, maxPathLength, limiter) match
          case Some(paths) => paths
          case None => List()
      }
    }.flatten
    case None => List()
  }

  if paths.nonEmpty then Some(paths) else None
*/

def isPathFinal(path: Path, problem: Problem): Boolean =
  path.head == problem.end

  /*
  val paths = getLinkedArticles(currentArticle, problem.language) match {
    case Some(articles) => {
      for {
        article <- articles
      } yield {
        visitedArticles = visitedArticles + article
        _solve(article +: currentPath, problem, maxPathLength, visitedArticles, limiter) match
          case Some(paths) => paths
          case None => List()
      }
    }.flatten
    case None => List()
  }
  */