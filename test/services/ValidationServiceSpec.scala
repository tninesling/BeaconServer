package tests

import models.UserData
import services.ValidationService

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class ValidationServiceSpec extends FlatSpec with Matchers {

  "The validation method" should "return an instance of Some[UserData] if password matches passwordConfirmation" in {
    val validateResult = ValidationService.validate("password", "password", "5555555555")
    validateResult shouldBe a [Some[_]]
    validateResult.get shouldBe a [UserData]
  }
  it should "return a None if password does not match passwordConfirmation" in {
    val validateResult = ValidationService.validate("password", "notPassword", "5555555555")
    validateResult shouldBe a [None.type]
  }
}
