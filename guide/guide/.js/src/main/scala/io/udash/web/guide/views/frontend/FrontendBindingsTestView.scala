package io.udash
package web.guide.views.frontend

import io.udash.css.CssView
import io.udash.utils.ChangeContext
import io.udash.web.guide.FrontendBindingsTestState
import org.scalajs.dom.ext.Color

case object FrontendBindingsTestViewFactory extends StaticViewFactory[FrontendBindingsTestState.type](() => new FrontendBindingsTestView)

final class FrontendBindingsTestView extends View with CssView {

  println("view init")

  ChangeContext.init()

  import scalatags.JsDom.all._

  //  def inContext[El <: Element](binding: Binding): scalatags.generic.Modifier[El] = { t: El =>
  //    ChangeContext.bind(t, binding)
  //    binding.applyTo(t)
  //  }

  private val prop = Property(1)

  private val r = div(
    h2("Bindings test"),
    p(
      "Test test"
    ),
    button(onclick := (() => addBind()))("Add bind")
  ).render

  private def addBind(): Unit = {
    val child = div(bind(prop)).render
    r.appendChild(child)
    child.onclick = _ => r.removeChild(child)
    child.onmouseenter = _ => child.style.backgroundColor = Color.Yellow
    child.onmouseout = _ => child.style.backgroundColor = null
    child.onclick = _ => r.removeChild(child)
  }

  override def getTemplate: Modifier = {
    println("view get template")
    r
  }

  override def onClose(): Unit = {
    ChangeContext.stop()
    println("view closed")
  }
}