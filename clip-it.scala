import scala.util.parsing.combinator._
import java.util.Date
import java.text.SimpleDateFormat

case class Entry(book: Book, metadata: Metadata, highlight: String)
case class Book(title: String, author: String)
case class Metadata(markType: String, where: Location, when: Date)
case class Location(data: String)

class ClipItParser extends JavaTokenParsers {
  override val whiteSpace = """[ \t\r]+""".r
  def eol = "\n"

  def words = rep("""[A-Za-z0-9'":,.+-]+""".r) ^^ (_.mkString(" "))

  def title = words ~ ("(" ~> words <~ ")" <~ eol) ^^ {
    case name ~ author => Book(name, author)
  }

  def meta = "-" ~> ("Bookmark" | "Highlight" | "Note") ~ location ~ ("| Added on" ~> timestamp) ^^ {
    case which ~ loc ~ when => Metadata(which, loc, when)
  }

  def location = words ^^ {
    case data => Location(data)
  }

  def time = """[0-9]+:[0-9]+:[0-9]+""".r

  def timestamp = words ^^ {
    case when =>
      new SimpleDateFormat("EEEE, d MMMM YY HH:mm:ss ZZZZ").parse(when)
  }

  def entry = ((title ~ (meta <~ eol <~ eol) ~ rep(".+".r)) | (title ~ meta)) ^^ {
    case book ~ metadata ~ highlight => Entry(book.asInstanceOf[Book], metadata.asInstanceOf[Metadata], highlight.asInstanceOf[List[String]].mkString(" "))
    case book ~ metadata => Entry(book.asInstanceOf[Book], metadata.asInstanceOf[Metadata], "")
  }
}

object Entrance extends ClipItParser {
  def main: String = {
    val source = scala.io.Source.fromFile("My Clippings.txt")
    val lines = source.mkString.substring(1)
    source.close()
//  val text = "Developing Backbone.js Applications (Addy Osmani)\n - Highlight Loc. 242-43  | Added on Monday, 3 December 12 19:00:12 Greenwich Mean Time\n\n languages when implementing patterns in their projects, there are many lessons we can"

    for (text <- lines.split("==========")) {
      parseAll(entry, text.trim) match {
        case Success(result, _) => println(result)
        case x => {
          println(x)
          return "dicks"
        }
      }

    }

    ""
  }
}

Entrance.main
