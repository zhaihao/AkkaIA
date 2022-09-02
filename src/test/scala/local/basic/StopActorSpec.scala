/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Kill, PoisonPill, Props}

import scala.concurrent.duration.DurationInt

/**
  * StopActorSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/7 23:32
  */
class StopActorSpec extends LocalSpec {

  // 自定一个 stop 消息，其实不需要，有 PoisonPill
  "context/system stop" in {
    val cat = system.actorOf(Props[StopActorSpec.Cat], "cat")
    logger.info(cat.toString())
    cat ! "hi"
    expectMsg("meow")
    cat ! "sleep"
    expectNoMessage()
  }
  // 正常结束
  "send PoisonPill" in {
    val cat = system.actorOf(Props[StopActorSpec.Cat], "cat")
    logger.info(cat.toString())
    cat ! "hi"
    expectMsg("meow")
    cat ! PoisonPill
    expectNoMessage()
  }

  // 异常结束，会抛出 ActorKilledException
  "send Kill" in {
    val cat = system.actorOf(Props[StopActorSpec.Cat], "cat")
    logger.info(cat.toString())
    cat ! "hi"
    expectMsg("meow")
    cat ! Kill
    expectNoMessage()
  }

  // 优雅关闭
  "gracefulStop" in {
    val cat = system.actorOf(Props[StopActorSpec.Cat], "cat")
    logger.info(cat.toString())
    cat ! "hi"
    expectMsg("meow")
    import akka.pattern.gracefulStop
    val eventualBoolean = gracefulStop(cat, 5.seconds)
    import system.dispatcher
    awaitAssert(eventualBoolean.map(_ ==> true))
  }
}

object StopActorSpec {
  class Cat extends Actor with ActorLogging {
    override def receive = {
      case "hi" => sender() ! "meow"
      case "sleep" =>
        log.info("zzz")
        context.stop(self)
    }
  }
}
