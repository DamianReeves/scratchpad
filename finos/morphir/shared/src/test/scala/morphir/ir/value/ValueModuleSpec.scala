package morphir.ir.value
import zio.test._
import zio.Scope

object ValueModuleSpec extends ZIOSpecDefault {
  def spec: ZSpec[TestEnvironment with Scope, Any] = suite("ValueModule Spec")(
    test("Some test")(
      assertTrue(1 == 1)
    )
  )
}
