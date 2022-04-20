package morphir.ir.value
import zio.Chunk
import morphir.ir.Name

sealed trait ValueCase[+TA, +VA, +AType[+_], +Self] { self =>
  def attributes: VA
}
object ValueCase {
  final case class LetDefinitionCase[+TA, +VA, +AType[+_], +Self](
      attributes: VA,
      valueName: Name,
      valueDefinition: Definition[TA, VA, AType, Self]
  ) extends ValueCase[TA, VA, AType, Self]
}

final case class Definition[+TA, +VA, +AType[+_], +Value](
    inputTypes: Chunk[(Name, VA, AType[TA])],
    outputType: AType[TA],
    body: Value
)
