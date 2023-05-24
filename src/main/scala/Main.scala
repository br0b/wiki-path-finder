import java.io.{BufferedWriter, File, FileWriter}
import scala.io.{BufferedSource, Source}
import scala.util.{Success, Try, Using}
import java.io._

import Problem._
import Path._
import BacktrackParameters._

object Main {
  /**
   * @param args argument line parameters there can be either
   *               * none - backtrack parameters are assigned their default values (3, 3)
   *               * two  - they have to be positive integers.
   *                        The first one is interpreted as maximal path length and
   *                        the other as the number of top results to output.
   */
  def main(args: Array[String]): Unit =
    val pathToInputFile = getPathToInputFileFromStdIn
    val pathToOutputFile = getPathToOutputFileFromStdIn

    val backtrackParameters = getBacktrackParametersFromCommandLineArguments(args) match
      case Right(backtrackParameters) => backtrackParameters
      case Left(errorMessage) =>
        println(errorMessage)
        return;

    Using(Source.fromFile(pathToInputFile)) { source =>
      val allFoundPaths: Set[Path] = {
        for
          line <- source.getLines().toSet
        yield
          getSolutionsForProblemInLine(line, backtrackParameters)
      }.flatten

      savePathsToFile(allFoundPaths, pathToOutputFile)
    }
}

/**
 * @param  args comand line arguments
 * @return either backtrack parameters or an error message
 */
def getBacktrackParametersFromCommandLineArguments(args: Array[String]): Either[String, BacktrackParameters] =
  val DefaultBacktrackParameters = BacktrackParameters(3, 3)

  args.length match
    case 0 => Right(DefaultBacktrackParameters)
    case 2 =>
      val maxPathLength = args(0).toIntOption match
        case Some(int) => int
        case None => return Left("Wrong maximum length of path.")
      val numberOfTopResultsToOutput = args(1).toIntOption match
        case Some(int) => int
        case None => return Left("Wrong number of top results to output.")
      Right(BacktrackParameters(maxPathLength, numberOfTopResultsToOutput))
    case _ => Left("Wrong number of command line arguments.")

/**
 * @param line representing a problem
 * @return     solution for the given problem
 */
def getSolutionsForProblemInLine(line: String, backtrackParameters: BacktrackParameters): Set[Path] = {
  val problem = getProblemFromString(line)
  print(s"Looking for paths from ${problem.start} to ${problem.end}... ")
  val solution = solve(
    getProblemFromString(line),
    backtrackParameters
  )

  if solution.nonEmpty
  then
    print(s"${solution.size} paths found.\n")
  else
    print("none found.\n")

  solution
}

/**
 * Save output to file. The fields are article titles are comma separated.
 *
 * @param paths            are to be saved to file
 * @param pathToOutputFile path to the file where the paths should be saved
 */
def savePathsToFile(paths: Set[Path], pathToOutputFile: String): Unit =
  val bufferedWriterToOutput = getBufferedWriterToOutputFile(pathToOutputFile)

  val sortedPaths = paths.toSeq.sorted

  if sortedPaths.nonEmpty
  then
    bufferedWriterToOutput.append(pathToString(sortedPaths.head))
  else
    return;

  for path <- sortedPaths.tail do
    bufferedWriterToOutput.append(s"\n${pathToString(path)}")

  bufferedWriterToOutput.close()

/**
 * Prompts user to input the absolute path to the input file.
 *
 * @return the absolute path to the output file
 */
def getPathToInputFileFromStdIn: String =
  print("Please enter the absolute path to the input file: ")
  scala.io.StdIn.readLine()

/**
 * Prompts user to input the absolute path to the output file.
 *
 * @return the absolute path to the output file
 */
def getPathToOutputFileFromStdIn: String =
  print("Please enter the absolute path to the output file: ")
  scala.io.StdIn.readLine()

/**
 * Get buffered writer to the output file. Clears the file before writing to it.
 *
 * @param pathToOutputFile path to the output file
 * @return the buffered writer
 */
def getBufferedWriterToOutputFile(pathToOutputFile: String): BufferedWriter =
  val bufferedWriter = new BufferedWriter(new FileWriter(pathToOutputFile))
  bufferedWriter.write("") // clear file
  bufferedWriter