package com.beacon.controllers

import models.Beacon
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
}
