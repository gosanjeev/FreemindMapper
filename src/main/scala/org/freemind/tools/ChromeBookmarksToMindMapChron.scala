package org.freemind.tools

import java.io.File
import scala.util.Random
import scala.xml.XML
import scala.collection.immutable.TreeMap
import org.json4s._
import org.json4s.native.JsonMethods._

/**
 * Created by sanjeev on 2/25/15.
 */
object ChromeBookmarksToMindMapChron {

  implicit val formats = DefaultFormats

  // Brings in default date formats etc.

  case class Node(name: Option[String],
                  `type`: Option[String],
                  `date_added`: Option[String],
                  `date_modified`: Option[String],
                  id: Option[String],
                  children: List[Node],
                  url: Option[String]
                   )

  case class BookMark_Bar(bookmark_bar: Node)

  case class Root(roots: BookMark_Bar)

  def recursiveList(ele: Node, lvl: Int, index: Int): scala.xml.Elem = {

    if (ele == null) return null

    val r = new Random()
    var node: scala.xml.Elem = null
    val random = r.nextInt()

    def epochTimeMillis(): String = {
      return "" + System.currentTimeMillis()
    }
    def idGen(): String = {
      "ID_" + random.abs
    }
    def position(): String = {
      if (lvl == 2 && index % 2 == 0) "left" else if (lvl == 2 && index % 2 == 1) "right" else null
    }
    def linkAbs(): String = {
      ele.url.getOrElse("")
    }
    def color(): String = lvl match {
      case 1 => "#000000"
      case 2 => "#0033ff"
      case 3 => "#00b439"
      case 4 => "#990000"
      case 5 => "#0000cc"
      case 6 => "#990099"
      case 7 => "#009900"
      case _ => "#111111"
    }
    def width(): String = "" + 8 / lvl
    def style(): String = {
      if (lvl == 2) "bezier" else null
    }
    def escaper(s: String): String = s.replaceAll(" ", "%20")
    def folded(f: Option[String]): String = {
      if (lvl >= 2 && f.getOrElse("") == "folder") "true" else null
    }

    node = <node COLOR={color} CREATED={ele.`date_added`.getOrElse(epochTimeMillis)} FOLDED={folded(ele.`type`)} ID={idGen} POSITION={position} LINK={escaper(linkAbs)} MODIFIED={ele.`date_modified`.getOrElse(epochTimeMillis)} STYLE="bubble" TEXT={ele.name.getOrElse("")}>
      <edge COLOR={color} STYLE={style} WIDTH={width}/>{if (!ele.children.isEmpty) for ((n, idx) <- ele.children.zipWithIndex) yield recursiveList(n, lvl + 1, idx)}
    </node>
    return node
  }

  def getSortedTreeMap(n: Node, m: TreeMap[Long, Node]): TreeMap[Long, Node] = {
    var tree = TreeMap.empty[Long, Node]
    if (n == null) return tree
    tree += (n.`date_added`.getOrElse("0").toLong -> n)
    if (!n.children.isEmpty) {
      for (c <- n.children) {
        tree = tree ++ getSortedTreeMap(c, m)
      }
    }
    tree = tree ++ m
    tree
  }

  def createMindMaps(inputFile: String, outputFile: String) = {

    val level = 1
    // Read the json bookmark file
    val lines = scala.io.Source.fromFile(inputFile).mkString
    // parse the json
    val json = parse(lines)
    // iterate over the json to generate the xml
    val root = json.extract[Root]
    //println(s"Bookmark: ${root}")
    // Obtain a sorted TreeMap
    val sortedEle: TreeMap[Long, Node] = getSortedTreeMap(root.roots.bookmark_bar, TreeMap.empty[Long, Node])
    // Create the MindMap
    sortedEle.keys.foreach{case k => println(s"${k}: ${sortedEle.get(k).get.name}")}
//    var xml = <map version="1.0.1">
//      {recursiveList(root.roots.bookmark_bar, level, 0)}
//    </map>
//    // Save the MindMap
//    XML.save(outputFile, xml, "UTF-8", false, null)
  }

  def main(args: Array[String]) {

    createMindMaps("/Users/sanjeev/Library/Application Support/Google/Chrome/Profile 1/Bookmarks", "/Users/sanjeev/Dropbox/MindMaps/Projects/Bookmarks-BV-Chron.mm")

  }
}