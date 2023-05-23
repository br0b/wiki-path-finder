import java.io.{BufferedReader, BufferedWriter, FileReader}

type Path = Seq[String]

trait PathInterface:
  def Path(start: String): Path
  def pathToString(path: Path): String

object Path extends PathInterface:
  override def Path(start: String): Path = start :: Nil

  override def pathToString(path: Path): String =
    s"(${_pathToString(path)})"


def _pathToString(path: Path): String =
  if path.size > 1
  then
    s"${path.head}, ${_pathToString(path.tail)}"
  else
    s"${path.head}"