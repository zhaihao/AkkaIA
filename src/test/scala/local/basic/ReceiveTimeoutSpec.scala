/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}

import scala.concurrent.duration.DurationInt

/**
  * ReceiveTimeoutSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/10 23:49
  */
class ReceiveTimeoutSpec extends LocalSpec {

  /**
   * actor 设置了 ReceiveTimeout 之后，
   * 如果在超过这个时间后扔没收到任何消息，
   * 则给自己发一个 ReceiveTimeout 消息
   */
  "setReceiveTimeout" in {
    val dog = system.actorOf(Props[ReceiveTimeoutSpec.Dog], "dog")
    dog ! "start"
    import system.dispatcher
    system.scheduler.scheduleWithFixedDelay(1.millis, 200.millis, dog, "scheduler")
    expectMsg("over")
  }
}

object ReceiveTimeoutSpec {
  class Dog extends Actor with ActorLogging {

    var c    = 0
    var fire = Actor.noSender

    override def receive = {
      case "start" =>
        log.info("start")
        fire = sender()
        context.setReceiveTimeout(50.millis)
      case ReceiveTimeout =>
        log.warning(s"idle: $c")
        c += 1
        if (c > 5) fire ! "over"

      case i => log.info(i.toString)
    }
  }
}
