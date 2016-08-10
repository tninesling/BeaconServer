package tests

import models.LoginData
import models.Point
import models.UserData
import services.MongoService
import services.UserService
import services.ValidationService

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import play.api.Configuration

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationServiceSpec extends FlatSpec with Matchers {
  val config: Config = ConfigFactory.load("test.conf")
  val configuration: Configuration = new Configuration(config)
  val testMongoService = new MongoService(configuration)
  val testUserService = new UserService(configuration, testMongoService)
  val testValidationService = new ValidationService(testUserService)

  testUserService.users.drop(false)

  // create test User
  testUserService.create("1231231234", "password", "email@email.com", "first", "last", Point(0.0, 0.0), "existingUserName")

  "The validateSignup method" should "return an instance of Some[UserData] if password matches passwordConfirmation, phoneNumber is formatted at 10 consecutive digits, and no User already has that phone number" in {
    val validateResult = testValidationService.validateSignup("password", "password", "5555555555")
    validateResult shouldBe a [Some[_]]
    validateResult.get shouldBe a [UserData]
  }
  it should "return a None if password does not match passwordConfirmation" in {
    val validateResult = testValidationService.validateSignup("password", "notPassword", "5555555555")
    validateResult shouldBe a [None.type]
  }
  it should "return a None if phone number is not formatted as 10 consecutive digits" in {
    val validateResult = testValidationService.validateSignup("password", "password", "555-555-5555")
    validateResult shouldBe a [None.type]
  }
  it should "return a None if an existing User has that phone number" in {
    val validateResult = testValidationService.validateSignup("password", "password", "1231231234")
    validateResult shouldBe a [None.type]
  }
  it should "return a None if an existing User has that username (unless the username is the default empty string)" in {
    val validateResult = testValidationService.validateSignup("password", "password", "1234567890", "", "", "", "existingUserName")
    validateResult shouldBe a [None.type]

    val validateResult2 = testValidationService.validateSignup("password", "password", "1234567890", "", "", "", "")
    validateResult2 shouldBe a [Some[_]]
    validateResult2.get shouldBe a [UserData]
  }

  "The validateLogin method" should "return an instance of Some[LoginData] if phone number is formatted as 10 consecutive digits and the user with that number has that password" in {
    val validateResult = testValidationService.validateLogin("password", "1231231234", false)
    validateResult shouldBe a [Some[_]]
    validateResult.get shouldBe a [LoginData]
  }
  it should "return a None if the phone number is not formatted as 10 consecutive digits" in {
    val validateResult = testValidationService.validateLogin("password", "123-456-7890", false)
    validateResult shouldBe a [None.type]
  }
  it should "return a None if there is no user with that phoneNumber" in {
    val validateResult = testValidationService.validateLogin("password", "0000000000", false)
    validateResult shouldBe a [None.type]
  }
  it should "return a None if the password is incorrect" in {
    val validateResult = testValidationService.validateLogin("notPassword", "1231231234", false)
    validateResult shouldBe a [None.type]
  }
}
