import java.io.{BufferedWriter, File, FileWriter}
import scala.io.{BufferedSource, Source}
import scala.util.{Success, Try, Using}
import java.io.*

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import Problem.*
import Path.*

@main def main(pathToInputFile: String, pathToOutputFile: String): Unit =
  val MaxPathLength = 3
  val MaxSearchTime = 5.minutes

  Using(Source.fromFile(pathToInputFile)) { source =>

    val allFoundPaths: Seq[Path] = {
      for line <- source.getLines().toSeq yield {
        val pathsFuture: Future[Option[Seq[Path]]] = Future.apply {
          solve(getProblemFromString(line), MaxPathLength)
        }

        Await.result(pathsFuture, MaxSearchTime) match
          case Some(paths) => paths
          case None => Seq()
      }
    }.flatten

    val bufferedWriterToOutput = getBufferedWriterToOutputFile(pathToOutputFile)
    savePathsToFile(allFoundPaths, bufferedWriterToOutput)
    bufferedWriterToOutput.close()
  }

def savePathsToFile(paths: Seq[Path], bufferedWriterToOutput: BufferedWriter): Unit =
  val sortedPaths = paths.sorted
  if sortedPaths.nonEmpty
  then
    bufferedWriterToOutput.write(pathToString(sortedPaths.head))
  else
    return;

  for path <- sortedPaths.tail do
    bufferedWriterToOutput.write(s"\n${pathToString(path)}")

def getBufferedWriterToOutputFile(pathToOutputFile: String): BufferedWriter =
  val bufferedWriter = new BufferedWriter(new FileWriter(pathToOutputFile, true))
  bufferedWriter.write("") // clear file
  bufferedWriter