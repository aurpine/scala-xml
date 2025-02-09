/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
**                                 Copyright 2008 Google Inc.           **
**                                 All Rights Reserved.                 **
\*                                                                      */

package scala
package xml

import scala.collection.Seq

/**
 * This singleton object contains the `apply` and `unapplySeq` methods for
 *  convenient construction and deconstruction. It is possible to deconstruct
 *  any `Node` instance (that is not a `SpecialNode` or a `Group`) using the
 *  syntax `case Elem(prefix, label, attribs, scope, child @ _*) => ...`
 */
object Elem {

  def apply(prefix: String | Null, label: String | Null, attributes: MetaData, scope: NamespaceBinding | Null, minimizeEmpty: Boolean, child: Node*): Elem =
    new Elem(prefix, label, attributes, scope, minimizeEmpty, child: _*)

  def unapplySeq(n: Node) = n match {
    case _: SpecialNode | _: Group => None
    case _                         => Some((n.prefix, n.label, n.attributes, n.scope, n.child.toSeq))
  }
}

/**
 * An immutable data object representing an XML element.
 *
 * Child elements can be other [[Elem]]s or any one of the other [[Node]] types.
 *
 * XML attributes are implemented with the [[scala.xml.MetaData]] base
 * class.
 *
 * Optional XML namespace scope is represented by
 * [[scala.xml.NamespaceBinding]].
 *
 *  @param prefix        namespace prefix (may be null, but not the empty string)
 *  @param label         the element name
 *  @param attributes1   the attribute map
 *  @param scope         the scope containing the namespace bindings
 *  @param minimizeEmpty `true` if this element should be serialized as minimized (i.e. "&lt;el/&gt;") when
 *                       empty; `false` if it should be written out in long form.
 *  @param child         the children of this node
 */
class Elem(
  override val prefix: String | Null,
  val label: String | Null,
  attributes1: MetaData,
  override val scope: NamespaceBinding | Null,
  val minimizeEmpty: Boolean,
  val child: Node*
) extends Node with Serializable {

  final override def doCollectNamespaces = true
  final override def doTransform = true

  override val attributes = MetaData.normalize(attributes1, scope)

  if (prefix == "")
    throw new IllegalArgumentException("prefix of zero length, use null instead")

  if (scope == null)
    throw new IllegalArgumentException("scope is null, use scala.xml.TopScope for empty scope")

  //@todo: copy the children,
  //  setting namespace scope if necessary
  //  cleaning adjacent text nodes if necessary

  override protected def basisForHashCode: Seq[Any] =
    prefix :: label :: attributes :: child.toList

  /**
   * Returns a new element with updated attributes, resolving namespace uris
   *  from this element's scope. See MetaData.update for details.
   *
   *  @param  updates MetaData with new and updated attributes
   *  @return a new symbol with updated attributes
   */
  final def %(updates: MetaData): Elem =
    copy(attributes = MetaData.update(attributes, scope, updates))

  /**
   * Returns a copy of this element with any supplied arguments replacing
   *  this element's value for that field.
   *
   *  @return a new symbol with updated attributes
   */
  def copy(
    prefix: String | Null = this.prefix,
    label: String | Null = this.label,
    attributes: MetaData = this.attributes,
    scope: NamespaceBinding | Null = this.scope,
    minimizeEmpty: Boolean = this.minimizeEmpty,
    child: Seq[Node] = this.child.toSeq): Elem = Elem(prefix, label, attributes, scope, minimizeEmpty, child: _*)

  /**
   * Returns concatenation of `text(n)` for each child `n`.
   */
  override def text = (child map (_.nn.text)).mkString
}
