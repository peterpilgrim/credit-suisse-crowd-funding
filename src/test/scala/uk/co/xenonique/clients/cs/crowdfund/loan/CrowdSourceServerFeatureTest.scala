/*******************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 by Peter Pilgrim, Milton Keynes, P.E.A.T UK LTD
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Creative Commons 3.0
 * Non Commercial Non Derivation Share-alike License
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Developers:
 * Peter Pilgrim -- design, development and implementation
 *               -- Blog: http://www.xenonique.co.uk/blog/
 *               -- Twitter: @peter_pilgrim
 *
 * Contributors:
 *
 *******************************************************************************/
// Commented out? Why? See ``STACKTRACE.md''

package uk.co.xenonique.clients.cs.crowdfund.loan

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

/**
  * The type CrowdSourceServerFeatureTest
  *
  * @author Peter Pilgrim (peter)
  */

class CrowdSourceServerFeatureTest  extends FeatureTest {
  override val server = new EmbeddedHttpServer(new CrowdSourceServer)

  "Platform service" should  {
    "greet the world politely" in {
      server.httpGet(
        path = "/hello",
        andExpect = Status.Ok,
        withBody = "Crowd source loan platform says hello"
      )
      /* ... */
    }
  }

  "Create loan request for a borrower" in {
    server.httpPost(
      path = "/loan/request",
      postBody =
        """
          |{"amount": 1000,"duration":1000}
        """.stripMargin,
      andExpect = Status.Ok,
      withJsonBody =
        """
          |{"id" : 1001 }
        """.stripMargin
    )
  }

  "Create loan request for a second borrower" in {
    server.httpPost(
      path = "/loan/request",
      postBody =
        """
          |{"amount": 2500,"duration":500}
        """.stripMargin,
      andExpect = Status.Ok,
      withJsonBody =
        """
          |{"id" : 1003 }
        """.stripMargin
    )
  }

  "Create loan offers bundle using example 1" in {
    val expectedJsonInputAndOutput = List(
      """
        |{"loanRequestId": 1001, "amount": 100, "apr": 5.0}
      """.stripMargin -> """
        |{ "id" : 8006004 }
      """.stripMargin,
      """
        |{"loanRequestId": 1001, "amount": 500, "apr": 8.6}
      """.stripMargin -> """
        |{ "id" : 8006006 }
      """.stripMargin
    )

    nap()

    for (jsonBody <- expectedJsonInputAndOutput) {
      server.httpPost(
        path = "/loan/offer",
        postBody = jsonBody._1,
        andExpect = Status.Ok,
        withJsonBody = jsonBody._2
      )
      nap()
    }
  }

  "Get the current loan offer bundle for the first borrower" in {
    server.httpGet(
      path = "/loan/current/1001",
      andExpect = Status.Ok,
      withJsonBody =
        """
          |{
          |  "loanRequestId" : 1001,
          |  "amount" : 600,
          |  "apr" : 8.0,
          |  "loanOffers" : [
          |    {
          |      "loanOfferId" : 8006004,
          |      "amount" : 100,
          |      "apr" : 5.0
          |    },
          |    {
          |      "loanOfferId" : 8006006,
          |      "amount" : 500,
          |      "apr" : 8.6
          |    }
          |  ]
          |}
        """.stripMargin
    )
  }

  "Create loan offer for lender with unknown request id" in {
    val requestId = scala.util.Random.nextInt(1000000) + 500000
    server.httpPost(
      path = "/loan/offer",
      postBody =
        s"""
          |{"loanRequestId": $requestId, "amount": 100, "apr": 5.0}
        """.stripMargin,
      andExpect = Status.NotFound
    )
  }

  "Retrieve current loan offer with unknown request id" in {
    val requestId = scala.util.Random.nextInt(1000000) + 500000
    server.httpGet(
      path = "/loan/current/"+requestId,
      andExpect = Status.NotFound
    )
  }

  def nap(): Unit = {
    Thread.sleep(150L)
  }
}
