/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml

import scala.collection.Seq

/**
 * This singleton object contains the `apply` and `unapply` methods for
 *  convenient construction and deconstruction.
 *
 *  @author  Burak Emir
 */
object Attribute {
  def unapply(x: Attribute) = x match {
    case PrefixedAttribute(_, key, value, next) => Some((key, value, next))
    case UnprefixedAttribute(key, value, next)  => Some((key, value, next))
    case _                                      => None
  }

  /** Convenience functions which choose Un/Prefixedness appropriately */
  def apply(key: String | Null, value: Seq[Node] | Null, next: MetaData): Attribute =
    new UnprefixedAttribute(key, value, next)

  def apply(pre: String | Null, key: String, value: String | Null, next: MetaData): Attribute =
    if (pre == null || pre == "") new UnprefixedAttribute(key, value, next)
    else new PrefixedAttribute(pre, key, value, next)

  def apply(pre: String | Null, key: String | Null, value: Seq[Node] | Null, next: MetaData): Attribute =
    if (pre == null || pre == "") new UnprefixedAttribute(key, value, next)
    else new PrefixedAttribute(pre, key, value, next)

  def apply(pre: Option[String], key: String | Null, value: Seq[Node] | Null, next: MetaData): Attribute =
    pre match {
      case None    => new UnprefixedAttribute(key, value, next)
      case Some(p) => new PrefixedAttribute(p, key, value, next)
    }
}

/**
 * The `Attribute` trait defines the interface shared by both
 *  [[scala.xml.PrefixedAttribute]] and [[scala.xml.UnprefixedAttribute]].
 *
 *  @author  Burak Emir
 */
abstract trait Attribute extends MetaData {
  def pre: String | Null // will be null if unprefixed
  val key: String | Null
  val value: Seq[Node] | Null
  val next: MetaData

  def apply(key: String | Null): Seq[Node] | Null
  def apply(namespace: String | Null, scope: NamespaceBinding, key: String | Null): Seq[Node] | Null
  def copy(next: MetaData): Attribute

  def remove(key: String | Null) =
    if (!isPrefixed && this.key == key) next
    else copy(next remove key)

  def remove(namespace: String | Null, scope: NamespaceBinding, key: String) =
    if (this.key == key && (scope getURI pre) == namespace) next
    else copy(next.remove(namespace, scope, key))

  def isPrefixed: Boolean = pre != null

  def getNamespace(owner: Node): String | Null

  def wellformed(scope: NamespaceBinding): Boolean = {
    val arg = if (isPrefixed) scope getURI pre else null
    (next(arg, scope, key) == null) && (next wellformed scope)
  }

  /** Returns an iterator on attributes */
  override def iterator: Iterator[MetaData] = {
    if (value == null) next.iterator
    else Iterator.single(this) ++ next.iterator
  }

  override def size: Int = {
    if (value == null) next.size
    else 1 + next.size
  }

  /**
   * Appends string representation of only this attribute to stringbuffer.
   */
  protected def toString1(sb: StringBuilder): Unit = {
    if (value == null)
      return
    if (isPrefixed)
      sb append pre append ':'

    sb append key append '='
    val sb2 = new StringBuilder()
    Utility.sequenceToXML(value, TopScope, sb2, stripComments = true)
    Utility.appendQuoted(sb2.toString, sb)
  }
}
