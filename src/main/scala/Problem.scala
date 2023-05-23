import com.google.common.util.concurrent.RateLimiter
import sttp.client4.*
import Path.*
import WikiApi.*

import scala.annotation.tailrec

case class Problem (
  language: String,
  start: String,
  end: String
)

trait ProblemInterface:
  def solve(problem: Problem, maxPathLength: Int): Set[Path]
  def getProblemFromString(string: String): Problem

object Problem extends ProblemInterface:
  override def getProblemFromString(string: String): Problem =
    val tuple = string.substring(1, string.length() - 1).split(",").toVector.map(_.trim)
    Problem(tuple(0), tuple(1), tuple(2))

  override def solve(problem: Problem, maxPathLength: Int): Set[Path] =
    if (problem.start == problem.end) return Set(Path(problem.end)) // corner case

    /**
     * @param pathsFound - solutions for the problem that have already been found
     * @param pathsToExplore - potential prefixes of solutions
     * @param problem - problem to solve
     * @param maxPathLength - maximal length of path
     * @param visitedArticles - articles that have already been visited
     * @param limiter - a limiter which limits get requests to the wiki API
     * @return
     */
    @tailrec def loop(
      pathsFound: Set[Path],
      pathsToExplore: Set[Path],
      problem: Problem,
      maxPathLength: Int,
      visitedArticles: Set[Article],
      limiter: RateLimiter): Set[Path] = {
      if (pathsToExplore.isEmpty) return pathsFound

      val newPathsToExplore: Set[Path] = for {
        path <- pathsToExplore.filter(_.size < maxPathLength)
        article <- linkedArticles(path.head, problem.language, limiter).diff(visitedArticles)
      } yield {
        // debug
        println(article +: path)
        article +: path
      }

      val newPathsFound = newPathsToExplore.filter(isPathFinal(_, problem))
      val newVisitedArticles = for path <- newPathsToExplore yield path.head

      loop(
        pathsFound ++ newPathsFound,
        newPathsToExplore,
        problem,
        maxPathLength,
        visitedArticles ++ newVisitedArticles,
        limiter
      )
    }

    val limiter = RateLimiter.create(150) // limit of 150 requests per second
    val pathsToExplore = Set[Path](Path(problem.start))

    val paths: Set[Path] = loop(Set(), pathsToExplore, problem, maxPathLength, Set(problem.start), limiter)

    paths.map(_.reverse)

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