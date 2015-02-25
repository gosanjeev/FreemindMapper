package org.freemind.tools

import java.io.File
import scala.util.Random
import scala.xml.XML

object CreateMaps {

  def recursiveList(f: File, initRef: String, lvl: Int, index: Int, filter: List[String]): scala.xml.Elem = {

    if (f == null || (filter != null && filter.contains(f.getName()))) return null

    val r = new Random()
    var node: scala.xml.Elem = null
    val random = r.nextInt()
    val pattern = initRef.r

    def epochTimeMillis(): String = { return "" + System.currentTimeMillis() }
    def idGen(): String = { "ID_" + random.abs }
    def position(): String = { if (lvl == 2 && index % 2 == 0) "left" else if (lvl == 2 && index % 2 == 1) "right" else null }
    def linkRel(): String = { pattern replaceFirstIn (f.getAbsolutePath(), "./") }
    def linkAbs(): String = { f.getAbsolutePath() }
    def color(): String = lvl match { case 1 => "#000000" case 2 => "#0033ff" case 3 => "#00b439" case 4 => "#990000" case 5 => "#0000cc" case 6 => "#990099" case 7 => "#009900" case _ => "#111111" }
    def width(): String = "" + 8 / lvl
    def style(): String = { if (lvl == 2) "bezier" else null }
    def escaper(s: String): String = s.replaceAll(" ", "%20")
    def folded(f: File): String = { if (lvl >= 2 && f.isDirectory()) "true" else null }

    node = <node COLOR={ color } CREATED={ epochTimeMillis } FOLDED={ folded(f) } ID={ idGen } POSITION={ position } LINK={ escaper(linkAbs) } MODIFIED={ epochTimeMillis } STYLE="bubble" TEXT={ f.getName() }>
             <edge COLOR={ color } STYLE={ style } WIDTH={ width }/>
             { if (f.isDirectory()) for ((file, idx) <- f.listFiles.zipWithIndex) yield recursiveList(file, initRef, lvl + 1, idx, filter) }
           </node>
    return node
  }

  def createMindMaps(rootFolder: String, outputFile: String, filter: List[String]) = {

    val file = new File(rootFolder)
    var files: Array[File] = null
    val r = new Random()
    val level = 1
    var xml = <map version="0.9.0">{ recursiveList(file, rootFolder, level, 0, filter) }</map>
    XML.save(outputFile, xml, "UTF-8", false, null)
  }

  def main(args: Array[String]) {

    val filter =  List(".DS_Store", ".git", ".classpath", ".project", ".settings", "MindMaps.mm", "workspaces")
    createMindMaps("/Data/", "/Users/sanjeev/Dropbox/MindMaps/Projects/Data-BV.mm", filter)

  }
}