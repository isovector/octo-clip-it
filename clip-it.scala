import scala.util.parsing.combinator._
import java.util.Date
import java.text.SimpleDateFormat

case class Entry(book: Book, metadata: Metadata, highlight: String)
case class Book(title: String, author: String)
case class Metadata(markType: String, where: Location, when: Date)
case class Location(data: String)

class ClipItParser extends JavaTokenParsers {
  def words = rep("""[A-Za-z0-9'":,.-]+""".r) ^^ (_.mkString(" "))

  def title = (words ~ (("(" ~> words) <~ ")")) ^^ {
    case name ~ author => Book(name, author)
  }

  def meta = "-" ~> ("Bookmark" | "Highlight") ~ location ~ ("| Added on" ~> timestamp) ^^ {
    case which ~ loc ~ when => Metadata(which, loc, when)
  }

  def location = words ^^ {
    case data => Location(data)
  }

  def time = """[0-9]+:[0-9]+:[0-9]+""".r

  def timestamp = (ident ~ ",") ~> wholeNumber ~ ident ~ wholeNumber ~ time <~ 
      "Greenwich Mean Time" ^^ {
    case date ~ month ~ year ~ when =>    
      new SimpleDateFormat("d MMMM YY HH:mm:ss").parse("%s %s %s %s".format(date, month, year, when))
  }

  def entry = title ~ meta ~ rep(".+".r) ^^ {
    case book ~ metadata ~ highlight => Entry(book, metadata, highlight.mkString(" "))
  }
}

object Entrance extends ClipItParser {
  def main: String = {
    val source = scala.io.Source.fromFile("My Clippings.txt")
    val lines = source.mkString
    source.close()
//  val text = "Developing Backbone.js Applications (Addy Osmani)\n - Highlight Loc. 242-43  | Added on Monday, 3 December 12 19:00:12 Greenwich Mean Time\n\n languages when implementing patterns in their projects, there are many lessons we can"

    for (text <- lines.split("==========")) {
      println(text)
      parseAll(entry, text) match {
        case Success(result, _) => println(result)
        case x => println(x)
      }
      return ""
    }

    "lol"
  }
}

Entrance.main
