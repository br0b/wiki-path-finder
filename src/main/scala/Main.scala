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
  val MaxPathLength = 6
  val MaxSearchTime = 10.seconds

  val bufferedWriterToOutput = getBufferedWriterToOutputFile(pathToOutputFile)
  Using(Source.fromFile(pathToInputFile)) { source =>

    for line <- source.getLines() do {
      val pathsFuture: Future[Option[List[Path]]] = Future.apply {
        solve(getProblemFromString(line), MaxPathLength)
      }

      Await.result(pathsFuture, MaxSearchTime) match
        case Some(paths) => for path <- paths do savePathToFile(path, bufferedWriterToOutput, pathToOutputFile)
        case None =>
    }
  }
  bufferedWriterToOutput.close()

def getBufferedWriterToOutputFile(pathToOutputFile: String): BufferedWriter =
  clearFile(pathToOutputFile)
  val bufferedWriter = new BufferedWriter(new FileWriter(pathToOutputFile, true))
  bufferedWriter.write("")
  bufferedWriter

def clearFile(pathToFile: String): Unit =
  val writer = new PrintWriter(pathToFile)
  writer.print("")
  writer.close()