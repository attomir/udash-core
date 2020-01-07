package io.udash.bindings

import org.scalajs.dom.Node

import scala.scalajs.js

package object modifiers {
  implicit class ElementExts(private val el: Node) extends AnyVal {
    def replaceChildren(oldChildren: Seq[Node], newChildren: Seq[Node]): Unit = {
      if (oldChildren == null || oldChildren.isEmpty) newChildren.foreach(el.appendChild)
      else {
        oldChildren.iterator.zip(newChildren.iterator).foreach { case (old, fresh) =>
          el.replaceChild(fresh, old)
        }
        oldChildren.iterator.drop(newChildren.size).foreach { child =>
          //todo can we avoid this check?
          js.UndefOr.any2undefOrA(child).foreach(el.removeChild)
        }
        newChildren.iterator.drop(oldChildren.size - 1).sliding(2)
          .foreach(s => if (s.size == 2) el.insertBefore(s(1), s(0).nextSibling))
      }
    }
  }
}
