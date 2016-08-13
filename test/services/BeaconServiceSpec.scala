package tests

import models.Beacon
import models.Point
import services.BeaconService
import services.MongoService

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import java.util.Date

import play.api.Configuration

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class BeaconServiceSpec extends FlatSpec with Matchers {
  val config: Config = ConfigFactory.load("test.conf")
  val configuration: Configuration = new Configuration(config)
  val testMongoService = new MongoService(configuration)
  val testBeaconService = new BeaconService(testMongoService)

  testBeaconService.beacons.drop(false)

  // create a set of Beacons
  testBeaconService.create("creator0", Point(0.0, 0.0))
  testBeaconService.create("creator1", Point(1.0, 1.0))
  testBeaconService.create("creator2", Point(2.0, 0.0))
  testBeaconService.create("creator3", Point(0.0, 3.0), "", "", "", new Date,
            new Date, 0, List(), 0.1, List("firstTag", "secondTag", "thirdTag"))
  testBeaconService.create("creator4", Point(10.0, 0.4), "", "", "", new Date,
            new Date, 0, List(), 0.1, List("firstTag", "secondTag", "fourthTag"))

  "The findNearbyBeacons method" should "return a Beacon if it exists" in {
    val resultList = Await.result(testBeaconService.findNearbyBeacons(0.0, 0.0,
                                  1, 1), Duration.Inf)
    resultList.size shouldBe 1
    resultList.head shouldBe a [Beacon]
  }
  it should "return multiple Beacons if several exist and the limit is greater than 1" in {
    val resultList = Await.result(testBeaconService.findNearbyBeacons(0.0, 0.0,
                                  200000, 10), Duration.Inf)
    resultList.size should be > 1
    resultList.foreach{
      _ shouldBe a [Beacon]
    }
  }
}
