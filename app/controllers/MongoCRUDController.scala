package controllers

import javax.inject.Inject
import models.Identity
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.GenericCRUD

import scala.concurrent.Future
import scala.util.{ Failure, Try }

class MongoCRUDController[E: Format, ID] @Inject() (val service: GenericCRUD[E, ID])(redirectUrl: ID => Call)(implicit identity: Identity[E, ID]) extends Controller {

  val DEFAULT_LIMIT = Seq("50")

  def create = Action.async(parse.json) { implicit request =>
    validateAndThen[E] {
      entity =>
        service.create(entity).map {
          case Right(id) => Created.withHeaders(LOCATION -> redirectUrl(id).url)
          case Left(err) => BadRequest(err)
        }
    }
  }

  def read(id: ID) = Action.async {
    service.read(id).map(_.fold(
      NotFound(s"Entity #$id not found")
    )(entity =>
        Ok(Json.toJson(entity))))
  }

  def update(id: ID) = Action.async(parse.json) { implicit request =>
    validateAndThen[E] {
      entity =>
        service.update(id, entity).map {
          case Right(id) => Ok.withHeaders(LOCATION -> redirectUrl(id).url)
          case Left(err) => BadRequest(err)
        }
    }
  }

  def delete(id: ID) = Action.async {
    service.delete(id).map {
      case Right(id) => Ok
      case Left(err) => BadRequest(err)
    }
  }

  def validateAndThen[T: Reads](t: T => Future[Result])(implicit request: Request[JsValue]) = {
    request.body.validate[T].map(t) match {
      case JsSuccess(result, _) =>
        result.recover { case e => BadRequest(e.getMessage()) }
      case JsError(err) =>
        Future.successful(BadRequest(Json.toJson(err.map {
          case (path, errors) => Json.obj("path" -> path.toString, "errors" -> JsArray(errors.flatMap(e => e.messages.map(JsString(_)))))
        })))
    }
  }

  def toError(t: (String, Try[JsValue])): JsObject = t match {
    case (paramName, Failure(e)) => Json.obj(paramName -> e.getMessage)
    case _ => Json.obj()
  }

}

