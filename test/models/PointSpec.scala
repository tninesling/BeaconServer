package tests

import models.Point

import org.scalatest.FlatSpec

class PointSpec extends FlatSpec {
  "A Point" should "throw IllegalArgumentException if latitude < -90" in {
    intercept[IllegalArgumentException] {
      val point = Point(-100.0, 0.0)
    }
  }

  it should "throw IllegalArgumentException if latitude > 90" in {
    intercept[IllegalArgumentException] {
      val point = Point(100.0, 0.0)
    }
  }

  it should "throw IllegalArgumentException if longitude < -180" in {
    intercept[IllegalArgumentException] {
      val point = Point(0.0, -200.0)
    }
  }

  it should "throw IllegalArgumentException if longitude > 180" in {
    intercept[IllegalArgumentException] {
      val point = Point(0.0, 200.0)
    }
  }
}
