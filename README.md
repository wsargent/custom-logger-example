# README

This is a "hello world" Play project with a custom logger.

The logger is configured with an `AppFieldBuilder` which has `ToValue` implicits for `Request` and `Result`, and has some tweaks on it:

```scala
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

    // def apply means we can skip _.apply(arg) and just use _(arg)
    logger.info("index: {}", _("request" -> request), _("candidateId" -> candidateId))
    
    val result = Ok(views.html.index())
    
    // logs the result
    logger.info("index: result {}", _("result" -> result))
    result
  }
}
```

The field builder looks like this, covering the request and the result:

```scala
trait AppFieldBuilder extends PresentationFieldBuilder with OptionValueTypes {

  implicit def requestHeaderLikeToValue[R <: RequestHeader]: ToValue[R] = { request =>
    ToObjectValue(
      keyValue("method" -> request.method),
      keyValue("path" -> request.path)
    )
  }

  implicit def responseHeaderToValue: ToValue[ResponseHeader] = { response =>
    ToObjectValue(
      keyValue("status" -> response.status),
      keyValue("reason" -> response.reasonPhrase)
    )
  }

  implicit def resultToValue: ToValue[Result] = { result =>
    ToObjectValue(
      keyValue("body" -> result.body),
      keyValue("responseHeader" -> result.header)
    )    
  }

  implicit def bodyToValue: ToValue[HttpEntity] = { entity =>
    ToObjectValue(
      keyValue("contentType" -> entity.contentType.toString()),
      keyValue("contentLength" -> entity.contentLength)
    )
  }
  
  def apply[V: ToValue](tuple: (String, V)) = keyValue(tuple)

  def apply[V: ToValue](key: String, value: V) = keyValue(key, value)

  def apply(e: Throwable) = keyValue(FieldConstants.EXCEPTION, e)
}
```

And the output when hitting `localhost:9000` looks like this:

```json
{
  "@timestamp": "2023-10-31T12:28:14.763086188-07:00",
  "@version": "1",
  "message": "index: request={method=GET, path=/}",
  "logger_name": "controllers.HomeController",
  "thread_name": "application-akka.actor.default-dispatcher-6",
  "level": "INFO",
  "level_value": 20000,
  "application.home": "/home/wsargent/work/custom-logger-example",
  "request": {
    "method": "GET",
    "path": "/"
  },
  "candidateId": null
}
```

When the DiagnosticLoggerMethod is used, additional source info is added to the JSON output:

```json
{
  "@timestamp": "2023-10-31T12:28:14.761300355-07:00",
  "@version": "1",
  "message": "index now=2023-10-31T19:28:14.753995780Z",
  "logger_name": "controllers.HomeController",
  "thread_name": "application-akka.actor.default-dispatcher-6",
  "level": "DEBUG",
  "level_value": 10000,
  "application.home": "/home/wsargent/work/custom-logger-example",
  "now": "2023-10-31T19:28:14.753995780Z",
  "sourceInfo": {
    "line": 26,
    "enclosing": "controllers.HomeController#index"
  }
}
```