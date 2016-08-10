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
}
