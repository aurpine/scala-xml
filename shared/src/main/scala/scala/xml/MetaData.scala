/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
**                                 Copyright 2008 Google Inc.           **
**                                 All Rights Reserved.                 **
\*                                                                      */

package scala
package xml

import Utility.sbToString
import scala.annotation.tailrec
import scala.collection.AbstractIterable
import scala.collection.Seq

object MetaData {
  /**
   * appends all attributes from new_tail to attribs, without attempting to
   * detect or remove duplicates. The method guarantees that all attributes
   * from attribs come before the attributes in new_tail, but does not
   * guarantee to preserve the relative order of attribs.
   *
   * Duplicates can be removed with `normalize`.
   */
  @tailrec
  def concatenate(attribs: MetaData, new_tail: MetaData): MetaData =
    if (attribs eq Null) new_tail
    else concatenate(attribs.next.nn, attribs copy new_tail)

  /**
   * returns normalized MetaData, with all duplicates removed and namespace prefixes resolved to
   *  namespace URIs via the given scope.
   */
  def normalize(attribs: MetaData, scope: NamespaceBinding | Null): MetaData = {
    def iterate(md: MetaData, normalized_attribs: MetaData, set: Set[String]): MetaData = {
      if (md eq Null) {
        normalized_attribs
      } else if (md.value eq null) {
        iterate(md.next.nn, normalized_attribs, set)
      } else {
        val key = getUniversalKey(md, scope).nn
        if (set(key)) {
          iterate(md.next.nn, normalized_attribs, set)
        } else {
          md copy iterate(md.next.nn, normalized_attribs, set + key)
        }
      }
    }
    iterate(attribs, Null, Set())
  }

  /**
   * returns key if md is unprefixed, pre+key is md is prefixed
   */
  def getUniversalKey(attrib: MetaData, scope: NamespaceBinding | Null) = attrib match {
    case prefixed: PrefixedAttribute     => { val s = scope.nn.getURI(prefixed.pre); (if(s == null) "" else s.nn) + prefixed.key }
    case unprefixed: UnprefixedAttribute => unprefixed.key
  }

  /**
   *  returns MetaData with attributes updated from given MetaData
   */
  def update(attribs: MetaData, scope: NamespaceBinding | Null, updates: MetaData): MetaData =
    normalize(concatenate(updates, attribs), scope)

}

/**
 * This class represents an attribute and at the same time a linked list of
 *  attributes. Every instance of this class is either
 *  - an instance of `UnprefixedAttribute key,value` or
 *  - an instance of `PrefixedAttribute namespace_prefix,key,value` or
 *  - `Null, the empty attribute list.
 *
 *  Namespace URIs are obtained by using the namespace scope of the element
 *  owning this attribute (see `getNamespace`).
 */
abstract class MetaData
  extends AbstractIterable[MetaData]
  with Iterable[MetaData]
  with Equality
  with Serializable {

  /**
   * Updates this MetaData with the MetaData given as argument. All attributes that occur in updates
   *  are part of the resulting MetaData. If an attribute occurs in both this instance and
   *  updates, only the one in updates is part of the result (avoiding duplicates). For prefixed
   *  attributes, namespaces are resolved using the given scope, which defaults to TopScope.
   *
   *  @param updates MetaData with new and updated attributes
   *  @return a new MetaData instance that contains old, new and updated attributes
   */
  def append(updates: MetaData, scope: NamespaceBinding = TopScope): MetaData =
    MetaData.update(this, scope, updates)

  /**
   * Gets value of unqualified (unprefixed) attribute with given key, null if not found
   *
   * @param  key
   * @return value as Seq[Node] if key is found, null otherwise
   */
  def apply(key: String | Null): Seq[Node] | Null

  /**
   * convenience method, same as `apply(namespace, owner.scope, key)`.
   *
   *  @param namespace_uri namespace uri of key
   *  @param owner the element owning this attribute list
   *  @param key   the attribute key
   */
  final def apply(namespace_uri: String | Null, owner: Node, key: String | Null): Seq[Node] | Null =
    apply(namespace_uri, owner.scope.nn, key)

  /**
   * Gets value of prefixed attribute with given key and namespace, null if not found
   *
   * @param  namespace_uri namespace uri of key
   * @param  scp a namespace scp (usually of the element owning this attribute list)
   * @param  k   to be looked for
   * @return value as Seq[Node] if key is found, null otherwise
   */
  def apply(namespace_uri: String | Null, scp: NamespaceBinding, k: String | Null): Seq[Node] | Null

  /**
   * returns a copy of this MetaData item with next field set to argument.
   */
  def copy(next: MetaData): MetaData

  /** if owner is the element of this metadata item, returns namespace */
  def getNamespace(owner: Node): String | Null

  def hasNext = (Null != next)

  def length: Int = length(0)

  def length(i: Int): Int = next.nn.length(i + 1)

  def isPrefixed: Boolean

  override def canEqual(other: Any) = other match {
    case _: MetaData => true
    case _           => false
  }
  override def strict_==(other: Equality) = other match {
    case m: MetaData => this.asAttrMap == m.asAttrMap
    case _           => false
  }
  protected def basisForHashCode: Seq[Any] = List(this.asAttrMap)

  /** filters this sequence of meta data */
  override def filter(f: MetaData => Boolean): MetaData =
    if (f(this)) copy(next.nn filter f)
    else next.nn filter f

  def reverse: MetaData =
    foldLeft(Null: MetaData) { (x, xs) =>
        xs.copy(x)
    }

  /** returns key of this MetaData item */
  def key: String | Null

  /** returns value of this MetaData item */
  def value: Seq[Node] | Null

  /**
   * Returns a String containing "prefix:key" if the first key is
   *  prefixed, and "key" otherwise.
   */
  def prefixedKey = this match {
    case x: Attribute if x.isPrefixed => x.pre.nn + ":" + key
    case _                            => key
  }

  /**
   * Returns a Map containing the attributes stored as key/value pairs.
   */
  def asAttrMap: Map[String, String] =
    (iterator map (x => (x.prefixedKey.nn, x.value.nn.text.nn))).toMap

  /** returns Null or the next MetaData item */
  def next: MetaData | Null

  /**
   * Gets value of unqualified (unprefixed) attribute with given key, None if not found
   *
   * @param  key
   * @return value in Some(Seq[Node]) if key is found, None otherwise
   */
  final def get(key: String | Null): Option[Seq[Node]] = { val k = apply(key); if(k == null) None else Some(k.nn) }

  /** same as get(uri, owner.scope, key) */
  final def get(uri: String | Null, owner: Node, key: String): Option[Seq[Node]] =
    get(uri, owner.scope.nn, key)

  /**
   * gets value of qualified (prefixed) attribute with given key.
   *
   * @param  uri namespace of key
   * @param  scope a namespace scp (usually of the element owning this attribute list)
   * @param  key to be looked fore
   * @return value as Some[Seq[Node]] if key is found, None otherwise
   */
  final def get(uri: String | Null, scope: NamespaceBinding, key: String): Option[Seq[Node]] =
    { val k = apply(uri, scope, key); if(k == null) None else Some(k.nn) }

  protected def toString1(): String = sbToString(toString1)

  // appends string representations of single attribute to StringBuilder
  protected def toString1(sb: StringBuilder): Unit

  override def toString(): String = sbToString(buildString)

  def buildString(sb: StringBuilder): StringBuilder = {
    sb append ' '
    toString1(sb)
    next.nn buildString sb
  }

  /**
   */
  def wellformed(scope: NamespaceBinding): Boolean

  def remove(key: String | Null): MetaData

  def remove(namespace: String | Null, scope: NamespaceBinding, key: String): MetaData

  final def remove(namespace: String | Null, owner: Node, key: String): MetaData =
    remove(namespace, owner.scope.nn, key)
}
