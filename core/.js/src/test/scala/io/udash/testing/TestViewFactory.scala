package io.udash.testing

import io.udash._
import org.scalajs.dom

class TestViewFactory[T <: TestState] extends ViewFactory[T] {
  val view = new TestView
  val presenter = new TestPresenter[T]
  var count = 0

  override def create(): (View, Presenter[T]) = {
    count += 1
    (view, presenter)
  }
}

class TestView extends ContainerView {
  import scalatags.JsDom.all._
  var lastChild: View = _
  var renderingCounter = 0
  var closed = false

  override def renderChild(view: Option[View]): Unit = {
    super.renderChild(view)
    lastChild = view.orNull
  }

  override def getTemplate: Modifier = {
    renderingCounter += 1
    Seq[Modifier](
      div(
        toString,
        childViewContainer
      ),
      dom.document.createTextNode(toString),
    )
  }

  override def onClose(): Unit = {
    closed = true
  }
}

class TestFinalView extends View {
  import scalatags.JsDom.all._
  var renderingCounter = 0

  override def getTemplate: Modifier = {
    renderingCounter += 1
    span(toString)
  }
}

class TestPresenter[T <: TestState] extends Presenter[T] {
  var lastHandledState: T = _
  var closed = false

  override def handleState(state: T): Unit = lastHandledState = state
  override def onClose(): Unit = closed = true
}