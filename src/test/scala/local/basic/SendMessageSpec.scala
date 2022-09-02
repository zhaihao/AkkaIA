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
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

/**
  * SendMessageSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 00:48
  */
class SendMessageSpec extends LocalSpec {
  private val cat = system.actorOf(Props[SendMessageSpec.Cat], "cat")

  /**
   * tell 的消息，对方可能会回复也可能会不回复
   */
  "send" in {
    cat ! "hi"
    expectMsg("meow")
  }

  /**
   * ask 消息，对方必须回复，如果对方不回复则会抛出异常
   */
  "ask" in {
    import akka.pattern.ask
    import system.dispatcher
    implicit val to = Timeout(5.seconds)
    val future      = cat ? "hi"
    // ask 回复会丢失消息类型
    future.foreach(i => logger.info(i.toString))
    // 回复会给到 future ，所以下面就接不到消息了
    expectNoMessage()
    // mapTo 转换消息类型
    (cat ? "hi").mapTo[String].foreach(i => logger.info(i))
  }

  /**
   * pipeTo 把一个 future 转发给另一个 actor，
   * 实现其实是 future.andThen tell,
   * 所以仍然需要 ExecutionContext
   */
  "pipeTo" in {
    import akka.pattern.{ask, pipe}
    import system.dispatcher
    implicit val to = Timeout(5.seconds)

    val future  = cat ? "hi"
    val future1 = future pipeTo cat
    future1.map(_ => ())
    expectNoMessage()
  }

  /**
   * forward 时，发送者会保留，
   * 实现其实是 tell(msg)(sender())
   */
  "forward" in {
    cat ! "woof"
    expectMsg("woof")
  }
}

object SendMessageSpec {
  class Cat extends Actor with ActorLogging {
    val dog = context.actorOf(Props[Dog], "dog")

    override def receive = {
      case "hi" =>
        log.info("meow")
        sender() ! "meow"
      case "woof" =>
        log.info(sender().toString())
        dog forward "woof"

      case _ => log.info("emm")
    }
  }

  class Dog extends Actor with ActorLogging {
    override def receive = { case "woof" =>
      log.info(sender().toString())
      log.info("woof")
      sender() ! "woof"
    }
  }
}
