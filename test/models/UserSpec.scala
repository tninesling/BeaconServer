package tests

import models.User

import java.util.Date

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class UserSpec extends FlatSpec with Matchers {
  "A User" should "be valid User if phone number follows the format xxxxxxxxxx" in {
    val user = User(new Date, "password", "5555555555", new Date)
    user shouldBe a [User]
  }
  it should "throw IllegalArgumentException if phone number does not follow the format xxxxxxxxxx" in {
    intercept[IllegalArgumentException] {
      val user = User(new Date, "password", "555-555-5555", new Date)
    }
  }
}
