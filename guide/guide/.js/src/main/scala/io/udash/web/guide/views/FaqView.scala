package io.udash.web.guide.views

import io.udash._
import io.udash.web.guide.FaqState

object FaqViewFactory extends StaticViewFactory[FaqState.type](() => new FaqView)

class FaqView extends View {
  import scalatags.JsDom.all._

  private val content = div(
    h2("FAQ"),
    p("TODO")
  )

  override def getTemplate: Modifier = content
}