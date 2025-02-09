/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package parsing

/**
 * Implementation of MarkupHandler that constructs nodes.
 *
 *  @author  Burak Emir
 */
abstract class ConstructingHandler extends MarkupHandler {
  val preserveWS: Boolean

  def elem(pos: Int, pre: String | Null, label: String | Null, attrs: MetaData,
           pscope: NamespaceBinding | Null, empty: Boolean, nodes: NodeSeq): NodeSeq =
    Elem(pre, label, attrs, pscope, empty, nodes: _*)

  def procInstr(pos: Int, target: String, txt: String) =
    ProcInstr(target, txt)

  def comment(pos: Int, txt: String) = Comment(txt)
  def entityRef(pos: Int, n: String) = EntityRef(n)
  def text(pos: Int, txt: String) = Text(txt)
}
