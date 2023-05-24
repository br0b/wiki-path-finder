import scala.annotation.tailrec
import com.google.common.util.concurrent.RateLimiter
import sttp.client4.*

import Path.*
import WikiApi.*
import BacktrackParameters._

/**
 * The class of problems this program solves, as specified in the project specification.
 *
 * @param language the language of articles for this specific problem
 * @param start the beginning of the path we want to find
 * @param end the end of the path we want to find
 */
case class Problem (
  language: LanguageCode,
  start: Article,
  end: Article
)

trait ProblemInterface:
  /**
   * Finds solution for the class of problem specified in the project specification.
   *
   * @return a set of numberOfTopResultsToOutput solutions to problem, whose length is at most maxPathLength
   */
  def solve(problem: Problem, backtrackParameters: BacktrackParameters): Set[Path]

  /**
   * Converts a string to an object of class Problem.
   *
   * @param problemString the string representing a problem object;
   *                      the fields are separated by commas and spaces
   * @return the problem object the problemString represents
   */
  def getProblemFromString(problemString: String): Problem

object Problem extends ProblemInterface:

  override def getProblemFromString(problemString: String): Problem =
    val tuple = problemString.split(",").toVector.map(_.trim)
    Problem(tuple(0), tuple(1), tuple(2))

  override def solve(problem: Problem, backtrackParameters: BacktrackParameters): Set[Path] =
    val limiter = RateLimiter.create(195) // limit of 195 requests per second.
                                          // Wikipedia's official rate limit is 200 requests per second.

    /**
     * A loop that is used to find the solution using a backtracking algorithm.
     * The pathsToExplore are paths that we know are not solutions,
     * but may be prefixes of paths that are.
     * All paths belonging to pathsToExplore have the same length.
     * For convenience, the order of articles in paths is reversed in this loop.
     *
     * @param solutionsFound    solutions for the problem that have already been found
     * @param pathsToExplore    potential solutions
     * @param visited           set of visited articles
     * @param currentPathLength the length of paths explored in this iteration
     * @return                  a set of solutions of size at most backtrackParameters.numberOfTopResultsToOutput
     */
    @tailrec def loop(solutionsFound: Set[Path], pathsToExplore: Set[Path],
      visited: Set[Article], currentPathLength: Int): Set[Path] = {
      if pathsToExplore.isEmpty
      then return getSortedTopElementsOfSet(solutionsFound, backtrackParameters.numberOfTopResultsToOutput)

      val newSolutionsFound = pathsToExplore.filter(isSolution(_, problem))

      val newVisited = for path <- pathsToExplore yield path.head

      val newPathsToExplore = (currentPathLength, newSolutionsFound.size) match
        case (backtrackParameters.maxPathLength, _) => Set()
        case (_, numberOfNewSolutionsFound)
          if numberOfNewSolutionsFound > backtrackParameters.numberOfTopResultsToOutput => Set()
        case (_, _) => getNewPathsToExplore(pathsToExplore, problem.language, visited, limiter)

      loop(
        solutionsFound ++ newSolutionsFound,
        newPathsToExplore,
        visited ++ newVisited,
        currentPathLength + 1
      )
    }

    // Solutions to our problem
    loop(
      Set(),
      Set[Path](Path(problem.start)),
      Set(),
      1).map(_.reverse) // Return a set of paths that are in correct order.

def getSortedTopElementsOfSet(set: Set[Path], numberOfElementsToTake: Int): Set[Path] = set
  .toSeq
  .sorted
  .take(numberOfElementsToTake)
  .toSet

/**
 * @param language language of articles
 * @param visited  visited article. We don't consider them anymore
 * @param limiter  a limiter passed on to WikiApiInterface for limiting rate of GET requests
 * @return         set of paths that might be prefixes of solutions and are longer than paths taken as input
 */
def getNewPathsToExplore(paths: Set[Path], language: LanguageCode,
                         visited: Set[Article], limiter: RateLimiter): Set[Path] =
  for {
    path <- paths
    // get only articles that have not yet been visited
    article <- linkedArticles(path.head, language, limiter).diff(visited)
  }
  yield {
    article +: path
  }

/**
 * Check if a given path is a solution to a given problem.
 *
 * @param  path the path we want to check
 * @param  problem we check if path is the solution of this problem
 * @return true if the path is a solution to the problem, false otherwise
 */
def isSolution(path: Path, problem: Problem): Boolean =
  path.head == problem.end