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

/** Default implementation of markup handler always returns `NodeSeq.Empty` */
abstract class DefaultMarkupHandler extends MarkupHandler {

  def elem(pos: Int, pre: String | Null, label: String | Null, attrs: MetaData,
           scope: NamespaceBinding | Null, empty: Boolean, args: NodeSeq) = NodeSeq.Empty

  def procInstr(pos: Int, target: String, txt: String) = NodeSeq.Empty

  def comment(pos: Int, comment: String): NodeSeq = NodeSeq.Empty

  def entityRef(pos: Int, n: String) = NodeSeq.Empty

  def text(pos: Int, txt: String) = NodeSeq.Empty

}
