/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props}

/**
  * LogSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/7 02:26
  */
class LogSpec extends LocalSpec {
  // 配置 akka log 使用 slf4j 输出
  // 注意生产环境 logback 需要使用 async，否则会对 actor 造成性能影响

  // language=HOCON
  override val config =
    """
      |akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
      |""".stripMargin

  "actor 使用 slf4j 输出 event log" in {
    val ping = system.actorOf(Props[LogSpec.Ping], "ping")
    ping ! "ping"
    expectMsg("pong")
  }
}

object LogSpec {
  class Ping extends Actor with ActorLogging {
    override def receive = { case "ping" =>
      log.info("pong")
      sender() ! "pong"
    }
  }
}
