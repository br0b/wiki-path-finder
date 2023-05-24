import com.google.common.util.concurrent.RateLimiter
import sttp.client4.*
import Path.*
import WikiApi.*

import scala.annotation.tailrec

/**
 * The class of problems this program solves, as specified in the project specification.
 *
 * @param language the language of articles for this specific problem
 * @param start the beginning of the path we want to find
 * @param end the end of the path we want to find
 */
case class Problem (
  language: String,
  start: String,
  end: String
)

trait ProblemInterface:
  /**
   * Finds solution for the class of problem specified in the project specification.
   *
   * @param problem to solve
   * @param maxPathLength we are looking for solutions, whose length does not exceed maxPathLength
   * @param numberOfTopResultsToOutput number of the shortest results to output
   * @return a set of numberOfTopResultsToOutput solutions to problem, whose length is at most maxPathLength
   */
  def solve(problem: Problem, maxPathLength: Int, numberOfTopResultsToOutput: Int): Set[Path]

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

  override def solve(problem: Problem, maxPathLength: Int, numberOfTopResultsToOutput: Int): Set[Path] =
    if (problem.start == problem.end) return Set(Path(problem.end)) // corner case

    val limiter = RateLimiter.create(195) // limit of 195 requests per second.
                                          // Wikipedia's official rate limit is 200 requests per second.

    /**
     * A loop that is used to find the solution using a backtracking algorithm.
     * The pathsToExplore are paths that we know are not solutions,
     * but may be prefixes of paths that are.
     * All paths belonging to pathsToExplore have the same length.
     * For convenience, the order of articles in paths is reversed in this loop.
     *
     * @param pathsFound - solutions for the problem that have already been found
     * @param pathsToExplore - potential prefixes of solutions that are not solutions
     * @return a set of solutions - numberOfTopResultsToOutput paths connecting problem.start to problem.end
     */
    @tailrec def loop(
      pathsFound: Set[Path],
      pathsToExplore: Set[Path],
      visited: Set[Article]): Set[Path] = {
      if (pathsToExplore.isEmpty || pathsFound.size >= numberOfTopResultsToOutput)
        return pathsFound.toSeq
          .sorted
          .take(numberOfTopResultsToOutput)
          .toSet

      val newPathsToExplore: Set[Path] = for {
        path <- pathsToExplore
        article <- linkedArticles(path.head, problem.language, limiter).diff(visited)
      } yield {
        article +: path
      }

      val newVisited = for path <- newPathsToExplore yield path.head
      val newPathsFound = newPathsToExplore.filter(isSolution(_, problem))

      loop(
        pathsFound ++ newPathsFound,
        newPathsToExplore.filter(_.size < maxPathLength), // Filtered paths, that have length maxPathLength,
                                                          // but are not solutions.
        visited ++ newVisited
      )
    }

    // At the beginning, there is only one path to consider - the singleton problem.start
    val pathsToExplore = Set[Path](Path(problem.start))

    // Solutions to our problem
    val paths: Set[Path] = loop(Set(), pathsToExplore, Set(problem.start))

    // Return a set of paths in the correct order.
    paths.map(_.reverse)

/**
 * Check if a given path is a solution to a given problem.
 *
 * @param path the path we want to check
 * @param problem we check if path is the solution of this problem
 * @return true if the path is a solution to the problem, false otherwise
 */
def isSolution(path: Path, problem: Problem): Boolean =
  path.head == problem.end