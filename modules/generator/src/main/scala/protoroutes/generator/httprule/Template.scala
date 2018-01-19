package protoroutes.generator.httprule

import fastparse.all._

final case class Template(
  segments: Segments,
  verb: Option[Verb]
) {

  def text: String =
    s"/${segments.text}${verb.map(_.text).getOrElse("")}"

}

object Template {

  val ! : P[Template] =
    P("/" ~ Segments.! ~ Verb.!.?)
      .map((Template.apply _).tupled)

  private[this] val parser: P[Template] =
    this.! ~ End

  def parse(s: String): Either[String, Template] =
    parser.parse(s) match {
      case success: Parsed.Success[Template] => Right(success.value)
      case failure: Parsed.Failure           => Left(failure.msg)
    }

}

final case class Segments(
  head: Segment,
  tail: Seq[Segment]
) {

  def text: String =
    toSeq.map(_.text).mkString("/")

  def toSeq: Seq[Segment] =
    head +: tail

}

object Segments {

  val ! : P[Segments] =
    Segment.!.rep(min = 1, sep = "/")
      .map({ case (head +: tail) => Segments(head, tail) })

}

sealed trait Segment {

  def text: String

}

object Segment {

  val ! : P[Segment] =
    RecursiveWildcard.! | Wildcard.! | Variable.! | Literal.!

  case object Wildcard extends Segment {

    val ! : P[Wildcard.type] =
      P("*").map(_ => Segment.Wildcard)

    def text: String =
      "*"

  }

  case object RecursiveWildcard extends Segment {

    val ! : P[RecursiveWildcard.type] =
      P("**").map(_ => Segment.RecursiveWildcard)

    def text: String =
      "**"

  }

  final case class Literal(text: String) extends Segment

  object Literal {

    val ! : P[Literal] =
      P(CharPred(c => c != '*' && c != '/' && c != '{' && c != '}').rep.!).map(Literal(_))

  }

  final case class Variable(fieldPath: FieldPath, segments: Option[Segments])
    extends Segment {

    def text: String =
      s"{${fieldPath.text}${segments.map("=" + _.text).getOrElse("")}}"

  }

  object Variable {

    val ! : Parser[Variable] =
      P("{" ~ FieldPath.! ~ ("=" ~ Segments.!).? ~ "}")
        .map((Variable.apply _).tupled)

  }

}

final case class FieldPath(head: String, tail: Seq[String]) {

  def idents: Seq[String] =
    head +: tail

  def text: String =
    idents.mkString(".")

}

object FieldPath {

  private[this] val letter: P[String] =
    P(CharIn('A' to 'Z', 'a' to 'z')).!

  private[this] val decimalDigit: P[String] =
    P(CharIn('0' to '9')).!

  private[this] val ident: P[String] =
    P(letter ~ (letter | decimalDigit | "_".!).rep).!

  val ! : P[FieldPath] =
    P(ident.!.rep(min = 1, sep = "."))
      .map({ case (head +: tail) => FieldPath(head, tail) })

}

final case class Verb(literal: String) {

  def text: String =
    s":$literal"

}

object Verb {

  val ! : P[Verb] =
    P(":" ~ AnyChar.rep.!)
      .map(Verb(_))

}
