package tests

import models.Point
import models.UserData
import services.MongoService
import services.UserService
import services.ValidationService

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import play.api.Configuration
import play.api.inject.ConfigurationProvider

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationServiceSpec extends FlatSpec with Matchers with MockitoSugar {
  val config: Config = ConfigFactory.load("test.conf")
  val configuration: Configuration = new Configuration(config)
  val testMongoService = new MongoService(configuration)
  val testUserService = new UserService(configuration, testMongoService)
  val testValidationService = new ValidationService(testUserService)

  testUserService.users.drop(false)

  "The validation method" should "return an instance of Some[UserData] if password matches passwordConfirmation, phoneNumber follows the format xxxxxxxxxx, and no User already has that phone number" in {
    val validateResult = testValidationService.validate("password", "password", "5555555555")
    validateResult shouldBe a [Some[_]]
    validateResult.get shouldBe a [UserData]
  }
  it should "return a None if password does not match passwordConfirmation" in {
    val validateResult = testValidationService.validate("password", "notPassword", "5555555555")
    validateResult shouldBe a [None.type]
  }
  it should "return a None if phone number does not follow the format xxxxxxxxxx" in {
    val validateResult = testValidationService.validate("password", "password", "555-555-5555")
    validateResult shouldBe a [None.type]
  }
  it should "return a None if an existing User has that phone number" in {
    testUserService.create("1231231234", "password", "email@email.com", "first", "last", Point(0.0, 0.0), "username")
    val validateResult = testValidationService.validate("password", "password", "1231231234")
    validateResult shouldBe a [None.type]
  }
}
