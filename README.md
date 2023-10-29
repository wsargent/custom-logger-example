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

```
2023-10-29 14:42:54 INFO  controllers.HomeController  index: request={method=GET, path=/}
2023-10-29 14:42:54 INFO  controllers.HomeController  index: result result={body={contentType=Some(text/html; charset=utf-8), contentLength=437}, responseHeader={status=200, reason=null}}
```