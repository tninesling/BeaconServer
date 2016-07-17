package tests

import models.User

import java.util.Date

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class UserSpec extends FlatSpec with Matchers {
  "A User" should "throw IllegalArgumentException if phone number does not follow the format 5555555555" in {
    intercept[IllegalArgumentException] {
      val user = User(new Date, "555-555-5555", "password", new Date)
    }
  }
}
