package theirs

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

class BusinessIntelligence12 extends Simulation {
  // Setup server, username and password
  val httpProtocol = http
    .baseUrl("http://localhost:7474")
    .acceptHeader("application/json")
    .basicAuth("neo4j", "benchmark")

  val query =
    """MATCH
        (message:Message)-[:HAS_CREATOR]->(creator:Person),
        (message)<-[like:LIKES]-(:Person)
      WHERE message.creationDate > $date
      WITH message, creator, count(like) AS likeCount
      WHERE likeCount > $likeThreshold
      RETURN
        message.id,
        message.creationDate,
        creator.firstName,
        creator.lastName,
        likeCount
      ORDER BY
        likeCount DESC,
        message.id ASC
      LIMIT 100
    """.stripMargin.replaceAll("\n", " ")

  val statements = """{"statements" : [{"statement" : "%s", "parameters" : { "date": 20110721220000000, "likeThreshold": 400 } }] }"""
    .format(query)

  val scn = scenario("theirs.BusinessIntelligence12")
    .during(30 ) {
        exec(
          http("BI-12 (theirs)")
            .post("/db/data/transaction/commit")
            .body(StringBody(statements))
            .asJson
            .check(status.is(200))
        )
    }

  setUp(scn.inject(atOnceUsers(8))).protocols(httpProtocol)

}

/*
    If you want to see the response from the server, add the following to the .check
            .check(bodyString.saveAs("BODY")))
            .exec(session => {
              val response = session("BODY").as[String]
              println(s"Response body: \n$response")
              session
            }
 */