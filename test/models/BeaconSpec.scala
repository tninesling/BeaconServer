package tests

import models.Beacon
import models.Point

import java.util.Date

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class BeaconSpec extends FlatSpec with Matchers {
  "A Beacon" should "throw IllegalArgumentException if the updatedAt time is before the createdAt time" in {
    intercept[IllegalArgumentException] {
      val beacon = Beacon("creator", new Point(0.0, 0.0), new Date(222222222), new Date(111111111))
    }
  }
  it should "throw IllegalArgumentException if the endTime is before the startTime" in {
    intercept[IllegalArgumentException] {
      val beacon = Beacon("creator", new Point(0.0, 0.0), new Date, new Date,
            "title", "address", "venueName", new Date(222222222), new Date(111111111))
    }
  }
}
