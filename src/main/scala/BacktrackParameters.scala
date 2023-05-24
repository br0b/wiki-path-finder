/**
 * Backtrack parameters for finding solutions
 *
 * @param maxPathLength we are looking for solutions, whose length does not exceed maxPathLength
 * @param numberOfTopResultsToOutput number of the shortest results to output
 */
case class BacktrackParameters(
  maxPathLength: Int,
  numberOfTopResultsToOutput: Int
)
