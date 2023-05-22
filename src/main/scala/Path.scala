import java.io.{BufferedReader, BufferedWriter, FileReader}

type Path = List[String]

trait PathInterface:
  def Path(start: String): Path
  def pathToString(path: List[String]): String
  def savePathToFile(path: Path, bufferedWriterToOutput: BufferedWriter, pathToOutputFile: String): Unit

object Path extends PathInterface:
  override def Path(start: String): Path = List(start)

  override def pathToString(path: List[String]): String =
    s"(${path.head}, ${listToString(path)})"

  override def savePathToFile(path: Path, bufferedWriterToOutput: BufferedWriter, pathToOutputFile: String): Unit =
    bufferedWriterToOutput.append(s"${pathToString(path)}\n")

def listToString(list: List[String]): String =
  s"${list.head}, ${listToString(list.tail)}"