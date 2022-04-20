package morphir.ir.types

import morphir.ir.Name

sealed trait TypeCase[+A, +Self]
object TypeCase {
  final case class VariableCase[+A](attributes:A, name:Name) extends TypeCase[A, Nothing]
}