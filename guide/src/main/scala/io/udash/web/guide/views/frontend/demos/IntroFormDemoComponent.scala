package io.udash.web.guide.views.frontend.demos

import java.lang.Float

import io.udash._
import io.udash.bootstrap.BootstrapStyles
import io.udash.bootstrap.UdashBootstrap.ComponentId
import io.udash.bootstrap.button.{ButtonStyle, UdashButton}
import io.udash.bootstrap.form.UdashInputGroup
import io.udash.web.guide.styles.partials.GuideStyles
import org.scalajs.dom.{Element, MouseEvent}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scalatags.JsDom

trait IntroFormDemoModel {
  def minimum: Int
  def between: Int
  def maximum: Int
}

class IntroFormDemoComponent extends Component {
  override def getTemplate: Element = IntroFormDemoViewPresenter()

  /** IntroFormDemoModel validator, checks if minimum <= between <= maximum */
  object IntroFormDemoModelValidator extends Validator[IntroFormDemoModel] {
    override def apply(element: IntroFormDemoModel)(implicit ec: ExecutionContext): Future[ValidationResult] = Future {
      val errors = mutable.ArrayBuffer[String]()
      if (element.minimum > element.maximum) errors += "Minimum is bigger than maximum!"
      if (element.minimum > element.between) errors += "Minimum is bigger than your value!"
      if (element.between > element.maximum) errors += "Maximum is smaller than your value!"

      if (errors.isEmpty) Valid
      else Invalid(errors.toSeq)
    }
  }

  /** Prepares model, view and presenter for demo component */
  object IntroFormDemoViewPresenter {
    import io.udash.web.guide.Context._
    def apply(): Element = {
      val model = ModelProperty[IntroFormDemoModel]
      model.subProp(_.minimum).set(0)
      model.subProp(_.between).set(10)
      model.subProp(_.maximum).set(42)

      model.addValidator(IntroFormDemoModelValidator)

      val presenter = new IntroFormDemoPresenter(model)
      new IntroFormDemoView(model, presenter).render
    }
  }

  class IntroFormDemoPresenter(model: ModelProperty[IntroFormDemoModel]) {
    private val random = new Random()

    /** Sets random values in demo model */
    def randomize() = {
      model.subProp(_.minimum).set(random.nextInt(100) - 25)
      model.subProp(_.between).set(random.nextInt(100))
      model.subProp(_.maximum).set(random.nextInt(100) + 25)
    }
  }

  class IntroFormDemoView(model: ModelProperty[IntroFormDemoModel], presenter: IntroFormDemoPresenter) {
    import io.udash.web.guide.Context._

    import JsDom.all._
    import scalacss.ScalatagsCss._

    private def i2s(i: Int) = i.toString
    private def s2i(s: String) = Float.parseFloat(s).toInt

    private val minimum = model.subProp(_.minimum).transform(i2s, s2i)
    private val between = model.subProp(_.between).transform(i2s, s2i)
    private val maximum = model.subProp(_.maximum).transform(i2s, s2i)

    val randomizeButton = UdashButton(
      buttonStyle = ButtonStyle.Primary,
      componentId = ComponentId("randomize")
    )("Randomize")

    randomizeButton.listen {
      case UdashButton.ButtonClickEvent(_) =>
        presenter.randomize()
    }

    def render: Element = div(id := "frontend-intro-demo", GuideStyles.get.frame, GuideStyles.get.useBootstrap)(
      UdashInputGroup()(
        UdashInputGroup.input(
          NumberInput.debounced(minimum)(id := "minimum").render
        ),
        UdashInputGroup.addon(" <= "),
        UdashInputGroup.input(
          NumberInput.debounced(between)(id := "between").render
        ),
        UdashInputGroup.addon(" <= "),
        UdashInputGroup.input(
          NumberInput.debounced(maximum)(id := "maximum").render
        ),
        UdashInputGroup.buttons(
          randomizeButton.render
        )
      ).render,
      h3("Is valid?"),
      p(
        bindValidation(model,
          _ => span(id := "valid")("...").render,
          {
            case Valid => span(id := "valid")("Yes").render
            case Invalid(errors) => span(id := "valid")(
              "No, because:",
              ul(GuideStyles.get.defaultList)(
                errors.map(e => li(e))
              )
            ).render
          },
          error => span(s"Validation error: $error").render
        )
      )
    ).render
  }
}
