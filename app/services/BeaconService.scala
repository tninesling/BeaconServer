package services

import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/** Provides CRUD operations for the Beacon model
  */
@Singleton
class BeaconService @Inject()(mongo: MongoService) {
  lazy val beacons = Await.result(mongo.beacons, Duration.Inf)

  def create(creator: String, location: Point, title: String = "", address = "",
             venueName: String = "", startTime: Date = new Date, endTime: Date = new Date,
             notifiedCount: Int = 0, notifiedUsers: List[String] = List(),
             range: Double = 0.1, tags: List[String] = List()) = {
    val createdAt = new Date
    val updatedAt = createdAt

    val newBeacon = Beacon(creator, location, createdAt, updatedAt, title, address,
                           venueName, startTime, endTime, notifiedCount + 1,
                           creator :: notifiedUsers, range, tags)
  }
}
