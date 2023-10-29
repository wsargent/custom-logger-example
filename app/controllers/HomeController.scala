package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  private val logger = logging.LoggerFactory.getLogger

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    val candidateId = request.getQueryString("candidateId")
    logger.info("index: {}", _("request" -> request), _("candidateId" -> candidateId))
    val result = Ok(views.html.index())
    logger.info("index: result {}", _("result" -> result))
    result
  }
}
