import java.io.{BufferedReader, BufferedWriter, FileReader}

trait PathInterface:
  def pathToString(path: List[String]): String

object Path extends PathInterface:
  override def pathToString(path: List[String]): String =
    s"(${path.head}, ${listToString(path)})"

  override def savePathToFile(bufferedWriterToOutput: BufferedWriter, pathToOutputFile: String,
    path: Either[String, List[String]]): Unit =
    path match {
      case Left(message) => bufferedWriterToOutput.append(s"$message\n")
      case Right(path) => bufferedWriterToOutput.append(s"${pathToString(path)}\n")
    }

def listToString(list: List[String]): String =
  s"${list.head}, ${listToString(list.tail)}"