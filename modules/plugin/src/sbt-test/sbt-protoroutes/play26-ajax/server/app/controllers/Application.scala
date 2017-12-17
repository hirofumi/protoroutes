package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class Application @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index())
  }

}
