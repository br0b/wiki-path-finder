import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ProblemTest extends AnyFunSuiteLike {

  val problems =
    Table(
      ("problem", "backtrackParameters", "expectedPaths"),
      (
        Problem("en", "Jason_Statham", "Poland"),
        BacktrackParameters(3, 3),
        Set(
          Seq("Jason_Statham", "Black_Sea", "Poland"),
          Seq("Jason_Statham", "YouTube", "Poland")
        )
      ),
      (
        Problem("pl", "Pierogi", "Kawior"),
        BacktrackParameters(3, 3),
        Set(
          Seq("Pierogi", "Jajko_(kulinaria)", "Kawior"),
          Seq("Pierogi", "Kuchnia_grecka", "Kawior"),
          Seq("Pierogi", "Kuchnia_rosyjska", "Kawior")
        )
      )
    )

  test("testSolve") {
    import Problem._

    forAll (problems) { (problem, backtrackParameters, expectedPaths) =>
      solve(problem, backtrackParameters) should equal (expectedPaths)
    }
  }

}
