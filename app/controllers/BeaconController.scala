package controllers

import models.Beacon
import models.BeaconData

import play.api.data.Forms.bigDecimal
import play.api.data.Forms.date
import play.api.data.Forms.list
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.text
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller

import javax.inject.Inject

class BeaconController @Inject()(config: Configuration) extends Controller {
  // grab host name and db name configurations from application.conf

  // if no host names found, default to localhost:27017
  lazy val hostURI = "mongodb://" + config.getString("localmongodb.server")
                                          .getOrElse("localhost:27017")
                                          .toString()

  // if no db name found, default to test
  lazy val dbName = config.getString("mongodb.db").getOrElse("test")

  /** This method handles an HTTP request to create a new Beacon
    */
  /*def create = Action.async { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson

    jsonBody.map { json =>
      val creator = json \ "creator"
    }
  }*/

  val createBeaconForm: Form[BeaconData] = Form(
    mapping(
      "creator" -> nonEmptyText,
      "latitude" -> bigDecimal,
      "longitude" -> bigDecimal,
      "title" -> text,
      "address" -> text,
      "venueName" -> text,
      "startTime" -> date,
      "endTime" -> date,
      "range" -> bigDecimal,
      "tags" -> list(text)
    )(BeaconData.apply)(BeaconData.unapply) verifying ("Failed form constraints",
      fields => fields match { case userData =>
        validationService.validateSignup(userData.password,
              userData.passwordConfirmation, userData.phoneNumber, userData.email,
              userData.firstName, userData.lastName, userData.username
        ).isDefined
      }
    )
  )

  def signup = Action.async {
    Future {
      Ok(views.html.signup(signUpForm))
    }
  }

  def signupPost = Action.async { implicit request =>
    Future {
      signUpForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, return form with errors
          BadRequest(views.html.signup(formWithErrors))
        },
        userData => {
          // binding success, form is bound successfully
          userService.create(userData.phoneNumber, userData.password, userData.email,
                userData.firstName, userData.lastName, new Point(0.0, 0.0), userData.username)
          Redirect(routes.HomeController.index).withSession("loggedIn" -> "true")
        }
      )
    }
  }

}
