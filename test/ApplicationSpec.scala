import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Application" should {
    "render index view" in {
      val result = route(app, FakeRequest(GET, "/")).get
      status(result) mustBe OK
      contentAsString(result) must include ("Todo app:")
    }

    "render not found view" in {
      val result = route(app, FakeRequest(GET, "/fake-url")).get
      status(result) mustBe SEE_OTHER
      val resultHeaders = headers(result)
      resultHeaders("Location") mustBe "/not-found"
    }

//    // TODO create a task
//    "add a mock task" in {
////      val taskData: Map[String, String] = Map(
////        "dueDate" -> "01-01-2000",
////        "description" -> "First day of the year 2000"
////      )
//      val result = route(app, FakeRequest(POST, "/add").withFormUrlEncodedBody(
//        ("dueDate", "01-01-2050"),
//        ("description", "First day of the year 2050")
//      )).get
//      status(result) mustBe OK
//    }

  }


}
