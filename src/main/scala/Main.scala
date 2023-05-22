import Problem._
import Path._

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.{BufferedSource, Source}
import scala.util.{Success, Try, Using}
import java.io.*

type ProblemSet = List[Either[String, Problem]]

@main def main(pathToInputFile: String, pathToOutputFile: String): Unit = {
  val bufferedWriterToOutput = getBufferedWriterToOutputFile(pathToOutputFile)
  Using(Source.fromFile(pathToInputFile)) { source =>
      for line <- source.getLines() do savePathToFile(bufferedWriterToOutput, pathToOutputFile,
                                                      solve(getProblemFromString(line)))
  }
  bufferedWriterToOutput.close()
}

def getBufferedWriterToOutputFile(pathToOutputFile: String): BufferedWriter =
  clearFile(pathToOutputFile)
  val bufferedWriter = new BufferedWriter(new FileWriter(pathToOutputFile, true))
  bufferedWriter.write("")
  bufferedWriter

def clearFile(pathToFile: String): Unit =
  val writer = new PrintWriter(pathToFile)
  writer.print("")
  writer.close()