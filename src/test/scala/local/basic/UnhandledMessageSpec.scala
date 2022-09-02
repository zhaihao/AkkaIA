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
  * UnhandledMessageSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 19:35
  */
class UnhandledMessageSpec extends LocalSpec {
  // language=HOCON
  override val config = """|akka.log-dead-letters = on # 开启死信日志
                           |""".stripMargin

  "actor 无法处理的消息" in {
    val my = system.actorOf(Props[UnhandledMessageSpec.MyActor], "my")
    my ! "234"
    my ! "123"
    expectMsg("123")
  }
}
object UnhandledMessageSpec {
  class MyActor extends Actor with ActorLogging {
    override def receive = { case "123" =>
      log.info("123")
      sender() ! "123"
    }
  }
}
