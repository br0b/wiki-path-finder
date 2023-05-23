import java.io.{BufferedWriter, File, FileWriter}
import scala.io.{BufferedSource, Source}
import scala.util.{Success, Try, Using}
import java.io.*

import scala.concurrent._
import scala.concurrent.duration._

import Problem.*
import Path.*

@main def main(pathToInputFile: String, pathToOutputFile: String): Unit =
  val MaxPathLength = 3
  val NumberOfTopResultsToOutput = 3
  val MaxSearchTime = 10.minutes // Maximal time for which the program tries to solve a problem.

  Using(Source.fromFile(pathToInputFile)) { source =>
    val allFoundPaths: Set[Path] = {
      for line <- source.getLines().toSet yield {
        import ExecutionContext.Implicits.global
        val pathsFuture: Future[Set[Path]] = Future.apply {
          solve(getProblemFromString(line), MaxPathLength, NumberOfTopResultsToOutput)
        }

        Await.result(pathsFuture, MaxSearchTime)
      }
    }.flatten

    savePathsToFile(allFoundPaths, pathToOutputFile)
  }

/** Save output to file. The fields are article titles are comma separated.
 * @param paths paths that are to be saved to file
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

/** Get buffered writer to the output file. Clears the file before writing to it.
 *
 * @param pathToOutputFile path to the output file
 * @return the buffered writer
 */
def getBufferedWriterToOutputFile(pathToOutputFile: String): BufferedWriter =
  val bufferedWriter = new BufferedWriter(new FileWriter(pathToOutputFile))
  bufferedWriter.write("") // clear file
  bufferedWriter