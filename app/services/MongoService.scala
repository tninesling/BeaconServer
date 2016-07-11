package com.beacon.services

import javax.inject.Inject

import play.api.Configuration

import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.ExecutionContext.Implicits.global

class MongoService @Inject()(config: Configuration) {
  // if no host names found, default to localhost:27017
  val hostURI = "mongodb://" + config.getString("localmongodb.server")
                                          .getOrElse("localhost:27017")
                                          .toString()

  // if no db name found, default to test
  val dbName = config.getString("mongodb.db").getOrElse("test")

  val driver = new MongoDriver
  val connection = driver.connection(List(hostURI))
  val database = connection.database(dbName)

  val beacons = database.map(_.collection("beacons"))
  val users = database.map(_.collection("users"))
}
