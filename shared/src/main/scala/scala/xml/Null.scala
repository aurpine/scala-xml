/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml

import Utility.isNameStart
import scala.collection.Iterator
import scala.collection.Seq

/**
 * Essentially, every method in here is a dummy, returning Zero[T].
 *  It provides a backstop for the unusual collection defined by MetaData,
 *  sort of a linked list of tails.
 *
 *  @author  Burak Emir
 */
case object Null extends MetaData {
  override def iterator = Iterator.empty
  override def size = 0
  override def append(m: MetaData, scope: NamespaceBinding = TopScope): MetaData = m
  override def filter(f: MetaData => Boolean): MetaData = this

  def copy(next: MetaData) = next
  def getNamespace(owner: Node) = null

  override def hasNext = false
  def next = null
  def key = null
  def value = null
  def isPrefixed = false

  override def length = 0
  override def length(i: Int) = i

  override def strict_==(other: Equality) = other match {
    case x: MetaData => x.length == 0
    case _           => false
  }
  override protected def basisForHashCode: Seq[Any] = Nil

  def apply(namespace: String | Null, scope: NamespaceBinding, key: String | Null) = null
  def apply(key: String | Null) =
    if (isNameStart(key.nn.head)) null
    else throw new IllegalArgumentException("not a valid attribute name '" + key + "', so can never match !")

  protected def toString1(sb: StringBuilder) = ()
  override protected def toString1(): String = ""

  override def toString(): String = ""

  override def buildString(sb: StringBuilder): StringBuilder = sb

  override def wellformed(scope: NamespaceBinding) = true

  def remove(key: String | Null) = this
  def remove(namespace: String | Null, scope: NamespaceBinding, key: String) = this
}
