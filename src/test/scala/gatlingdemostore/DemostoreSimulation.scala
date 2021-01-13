package gatlingdemostore

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class DemostoreSimulation extends Simulation {

	val domain = "demostore.gatling.io"

	val httpProtocol = http
		.baseUrl("https://" + domain)

	val categoryFeeder = csv("data/categoryDetails.csv").random

	object CmsPages {
		def homepage = {
			exec(http("Load Home Page")
				.get("/")
				.check(status.is(200))
				.check(regex("<title>Gatling Demo-Store</title>").exists)
				.check(css("#_csrf", "content").saveAs("csrfValue")))
		}

		def aboutUs = {
			exec(http("Load About Us Page")
				.get("/about-us")
				.check(status.is(200))
				.check(substring("About Us"))
			)
		}
	}

	object Catalog {
		object Category {
			def view = {
				feed(categoryFeeder)
					.exec(http("Load Category Page - ${categoryName}")
						.get("/category/${categorySlug}")
						.check(status.is(200))
						.check(css("#CategoryName").is("${categoryName}"))
					)
			}
		}
	}

	val scn = scenario("DemostoreSimulation")
		.exec(CmsPages.homepage)
		.pause(2)
		.exec(CmsPages.aboutUs)
		.pause(2)
		.exec(Catalog.Category.view)
		.pause(2)
		.exec(http("Load Product Page")
			.get("/product/black-and-red-glasses"))
		.pause(2)
		.exec(http("Add Product to Cart")
			.get("/cart/add/19"))
		.pause(2)
		.exec(http("View Cart")
			.get("/cart/view"))
		.pause(2)
		.exec(http("Login User")
			.post("/login")
			.formParam("_csrf", "${csrfValue}")
			.formParam("username", "user1")
			.formParam("password", "password1"))
		.pause(2)
		.exec(http("Checkout")
			.get("/cart/checkout"))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}