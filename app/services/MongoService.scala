package services

import javax.inject.Inject
import javax.inject.Singleton

import play.api.Configuration

import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MongoService @Inject()(config: Configuration) {
  // if no host names found, default to localhost:27017
  lazy val hostURI = config.getString("mongodb.server")
                           .getOrElse("localhost:27017")
                           .toString()

  // if no db name found, default to test
  lazy val dbName = config.getString("mongodb.db").getOrElse("test")

  lazy val driver = new MongoDriver
  lazy val connection = driver.connection(List(hostURI))
  lazy val database = connection.database(dbName)

  lazy val beacons = database.map(_.collection("beacons"))
  lazy val users = database.map(_.collection("users"))
}
