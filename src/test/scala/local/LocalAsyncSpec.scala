/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.BeforeAndAfterAll
import test.BaseAsyncSpec

/**
  * LocalAsyncSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/7 01:21
  */
trait LocalAsyncSpec extends BaseAsyncSpec with BeforeAndAfterAll with TestKitBase with ImplicitSender with StrictLogging {
  val config: String = ""
  override implicit lazy val system =
    ActorSystem("spec", ConfigFactory.parseString(config + "\n" + """akka.loggers = ["akka.event.slf4j.Slf4jLogger"]"""))
  override protected def afterAll(): Unit = shutdown()
}
