/*
 * Copyright (c) 2016-present, Total Location Test Paragraph.
 * All rights reserved.
 *
 * This file is part of Where@. Where@ is free software:
 * you can redistribute it and/or modify it under the terms of
 * the GNU General Public License (GPL), either version 3
 * of the License, or (at your option) any later version.
 *
 * Where@ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For more details,
 * see the full license at <http://www.gnu.org/licenses/gpl-3.0.en.html>
 */

package io.whereat.integration

import java.net.URI

import io.whereat.integration.support.{TestConfig, TestWebClient}
import io.whereat.MainTrait
import io.whereat.model.{ExpiringLocationJsonProtocol, ExpiringLocation, LocationJsonProtocol, Location}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, ShouldMatchers, WordSpec}
import spray.json._

import scala.language.postfixOps

class ClientIntegrationTest
  extends WordSpec
    with Matchers
    with ShouldMatchers
    with ExpiringLocationJsonProtocol {

  object TestMain extends MainTrait with TestConfig

  "The server" should {
    "relay a location from one client to another" in {
      TestMain.run.futureValue(PatienceConfiguration.Timeout(Span(2, Seconds)))

      val serverUri: URI = new URI(s"ws://${TestMain.httpInterface}:${TestMain.httpPort}/locations/websocket")

      val clientA: TestWebClient = new TestWebClient(serverUri)
      val clientB: TestWebClient = new TestWebClient(serverUri)
      clientA.connect()
      clientB.connect()

      val sentLocation: ExpiringLocation = ExpiringLocation(
        lat = 40.7092529,
        lon = 40.7092529,
        ttl = 5000
      )
      clientA.sendMessage(sentLocation.toJson.toString)
      clientA.disconnect()

      eventually(clientB.messages.map(_.parseJson.convertTo[ExpiringLocation])
        should contain(sentLocation))

      clientB.disconnect()
    }
  }

}
