package org.freemind.tools

import scala.util.Random
import scala.xml.XML
import scala.collection.immutable.TreeMap
import org.json4s._
import org.json4s.native.JsonMethods._
import org.joda.time._
import org.joda.time.chrono._

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

  def recursiveList(sortedEle: TreeMap[Int, List[Node]], ele: Node, l: Int, idx: Int): scala.xml.Elem = {

    if (sortedEle.isEmpty) return null
    val r = new Random()
    var node: scala.xml.Elem = null
    val random = r.nextInt()
    var pos = idx

    def epochTimeMillis(): String = {
      return "" + System.currentTimeMillis()
    }
    def idGen(): String = {
      "ID_" + random.abs
    }
    def position(lvl: Int): String = {
      pos = pos + 1
      if (lvl == 2 && pos % 2 == 0) "left" else if (lvl == 2 && pos % 2 == 1) "right" else null
    }
    def color(lvl: Int): String = lvl match {
      case 1 => "#000000"
      case 2 => "#0033ff"
      case 3 => "#00b439"
      case 4 => "#990000"
      case 5 => "#0000cc"
      case 6 => "#990099"
      case 7 => "#009900"
      case _ => "#111111"
    }
    def width(lvl: Int): String = "" + 8 / lvl
    def style(lvl: Int): String = {
      if (lvl == 2) "bezier" else null
    }
    def escaper(s: String): String = s.replaceAll(" ", "%20")
    def folded(f: Option[String], lvl: Int): String = {
      if (lvl >= 2 && f.getOrElse("") == "folder") "true" else null
    }

    node = <node COLOR={color(l)} CREATED={ele.`date_added`.getOrElse(epochTimeMillis)} FOLDED={folded(ele.`type`,l)} ID={idGen} POSITION={position(l)} LINK={escaper(ele.url.getOrElse(""))} MODIFIED={ele.`date_modified`.getOrElse(epochTimeMillis)} STYLE="bubble" TEXT={ele.name.getOrElse("")}>
      <edge COLOR={color(l)} STYLE={style(l)} WIDTH={width(l)}/>{
      for(
         k <- sortedEle.keys;
         lst = sortedEle.get(k).get
      )yield (<node COLOR={color(l+1)} CREATED={epochTimeMillis} FOLDED="true" ID={idGen} POSITION={position(l+1)} LINK={escaper("")} MODIFIED={epochTimeMillis} STYLE="bubble" TEXT={k.toString}>
        <edge COLOR={color(l+1)} STYLE={style(l+1)} WIDTH={width(l+1)}/>{
        for (n <- lst) yield(
          <node COLOR={color(l+2)} CREATED={n.`date_added`.getOrElse(epochTimeMillis)} FOLDED="true" ID={idGen} POSITION="left" LINK={escaper(n.url.getOrElse(""))} MODIFIED={n.`date_modified`.getOrElse(epochTimeMillis)} STYLE="bubble" TEXT={n.name.getOrElse("")}></node>
          )
        }</node>)
      }</node>
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

  def getSortedMonthlyTreeMap(sortedTree: TreeMap[Long, Node]): TreeMap[Int, List[Node]] = {
    var tree = TreeMap.empty[Int, List[Node]]
    if (sortedTree.isEmpty) return tree
    sortedTree.keys.foreach { case k =>
      val ele = sortedTree.get(k).get
      val eleYear  = new DateTime((k/1000000L - 11644473600L)*1000).getYear
      val eleMonth = new DateTime((k/1000000L - 11644473600L)*1000).getMonthOfYear
      val yyyyMM = eleYear * 100 + eleMonth
      val treeVal = tree.getOrElse(yyyyMM, List.empty[Node])
      if (treeVal.nonEmpty) {
        tree += yyyyMM -> (treeVal :+ ele)
      } else {
        tree += yyyyMM -> List(ele)
      }
    }
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
    // Obtain a sorted TreeMap
    val sortedEle: TreeMap[Long, Node] = getSortedTreeMap(root.roots.bookmark_bar, TreeMap.empty[Long, Node])
    // Create the MindMap
    val sortedMonth: TreeMap[Int, List[Node]] = getSortedMonthlyTreeMap(sortedEle)

    var xml = <map version="1.0.1">
      {recursiveList(sortedMonth, root.roots.bookmark_bar, level, 0)}
    </map>
    // Save the MindMap
    XML.save(outputFile, xml, "UTF-8", false, null)
  }

  def main(args: Array[String]) {

    createMindMaps("/Users/sanjeev/Library/Application Support/Google/Chrome/Profile 1/Bookmarks", "/Users/sanjeev/Dropbox/MindMaps/Projects/Bookmarks-BV-Chron.mm")

  }
}