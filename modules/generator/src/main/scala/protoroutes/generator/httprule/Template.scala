package protoroutes.generator.httprule

final case class Template(
  segments: Segments,
  verb: Option[Verb]
) {

  def text: String =
    s"/${segments.text}${verb.map(_.text).getOrElse("")}"

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

sealed trait Segment {

  def text: String

}

object Segment {

  case object Wildcard extends Segment {

    def text: String =
      "*"

  }

  case object RecursiveWildcard extends Segment {

    def text: String =
      "**"

  }

  final case class Literal(text: String) extends Segment

  final case class Variable(fieldPath: FieldPath, segments: Option[Segments])
    extends Segment {

    def text: String =
      s"{${fieldPath.text}${segments.map("=" + _.text).getOrElse("")}}"

  }

}

final case class FieldPath(head: String, tail: Seq[String]) {

  def idents: Seq[String] =
    head +: tail

  def text: String =
    idents.mkString(".")

}

final case class Verb(literal: String) {

  def text: String =
    s":$literal"

}
