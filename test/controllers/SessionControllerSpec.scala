package tests

import controllers.SessionController
import services.MongoService
import services.UserService
import services.ValidationService

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import play.api.Configuration
import play.api.i18n.MessagesApi

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar

class SessionControllerSpec extends FlatSpec with Matchers with MockitoSugar {
  /*val config: Config = ConfigFactory.load("test.conf")
  val configuration: Configuration = new Configuration(config)
  val testMongoService = new MongoService(configuration)
  val testUserService = new UserService(configuration, testMongoService)
  val testValidationService = new ValidationService(testUserService)*/
  val mockMessagesApi = mock[MessagesApi]
  val mockUserService = mock[UserService]
  val mockValidationService = mock[ValidationService]
  val testSessionController = new SessionController(mockMessagesApi,
                                                    mockUserService,
                                                    mockValidationService)

  "A token" should "have at least 5 characters" in {
    testSessionController.newToken.length should be >= 5
  }
}
