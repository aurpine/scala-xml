/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml

/**
 * This object provides an extractor method to match a qualified node with
 *  its namespace URI
 *
 *  @author  Burak Emir
 */
object QNode {
  def unapplySeq(n: Node) = Some((n.scope.nn.getURI(n.prefix), n.label, n.attributes, n.child.toSeq))
}
