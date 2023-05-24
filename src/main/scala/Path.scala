import java.io.{BufferedReader, BufferedWriter, FileReader}
import WikiApi._

/**
 *  A path, where vertices are wikipedia articles and edges are links connecting them.
 *  We define path length as the number of vertices it contains.
 *  The first article is the beginning, the last one is the end.
 */
type Path = Seq[Article]

trait PathInterface:
  /**
   * Create a path containing a single article and no edges.
   *
   * @param article the single article belonging to the path
   * @return        the new path
   */
  def Path(article: Article): Path

  /**
   * Convert a path to string.
   *
   * @param  path The path to be converted
   * @return the string representing the path
   */
  def pathToString(path: Path): String

object Path extends PathInterface:
  override def Path(article: Article): Path = article :: Nil

  override def pathToString(path: Path): String =
    // Create the string using recursion.
    if path.size > 1
    then
      s"${path.head}, ${pathToString(path.tail)}"
    else
      s"${path.head}"