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
  val MaxSearchTime = 5.minutes

  Using(Source.fromFile(pathToInputFile)) { source =>

    val allFoundPaths: Set[Path] = {
      for line <- source.getLines().toSet yield {
        import ExecutionContext.Implicits.global
        val pathsFuture: Future[Set[Path]] = Future.apply {
          solve(getProblemFromString(line), MaxPathLength)
        }

        Await.result(pathsFuture, MaxSearchTime)
      }
    }.flatten

    val bufferedWriterToOutput = getBufferedWriterToOutputFile(pathToOutputFile)
    savePathsToFile(allFoundPaths, bufferedWriterToOutput)
    bufferedWriterToOutput.close()
  }

def savePathsToFile(paths: Set[Path], bufferedWriterToOutput: BufferedWriter): Unit =
  val sortedPaths = paths.toSeq.sorted
  if sortedPaths.nonEmpty
  then
    bufferedWriterToOutput.append(pathToString(sortedPaths.head))
  else
    return;

  for path <- sortedPaths.tail do
    bufferedWriterToOutput.append(s"\n${pathToString(path)}")

def getBufferedWriterToOutputFile(pathToOutputFile: String): BufferedWriter =
  val bufferedWriter = new BufferedWriter(new FileWriter(pathToOutputFile))
  bufferedWriter.write("") // clear file
  bufferedWriter