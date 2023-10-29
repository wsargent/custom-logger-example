package logging

import com.tersesystems.echopraxia.plusscala.api.PresentationFieldBuilder
import com.tersesystems.echopraxia.spi.FieldConstants
import play.api.mvc._
import com.tersesystems.echopraxia.plusscala.api.OptionValueTypes
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.http.HttpEntity

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
  
  implicit def jsValueToValue: ToValue[JsValue] = { value =>
    ToValue(Json.stringify(value))
  }

  def apply[V: ToValue](tuple: (String, V)) = keyValue(tuple)

  def apply[V: ToValue](key: String, value: V) = keyValue(key, value)

  def apply(e: Throwable) = keyValue(FieldConstants.EXCEPTION, e)
}

object AppFieldBuilder extends AppFieldBuilder