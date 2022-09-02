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
  * ActorLifeCycleSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/7 01:28
  */
class ActorLifeCycleSpec extends LocalSpec {

  "观察actor的生命周期" in {
    logger.info("123")
    val c = system.actorOf(Props[ActorLifeCycleSpec.Child], "child")
    c ! new Exception("let it crash!")
  }

}
object ActorLifeCycleSpec {
  class Child extends Actor with ActorLogging {

    override def preStart() = log.info("preStart: actor 创建完，未开始处理消息之前")
    override def postStop() = log.info("postStop: actor 停止后，此方法触发能够确定给该actor的消息将被送到 dead letter")

    override def preRestart(reason: Throwable, message: Option[Any]) = {
      log.info("preRestart: 重启前，super 会停止所有子child，并调用 postStop")
      super.preRestart(reason, message)
    }

    override def postRestart(reason: Throwable) = {
      log.info("postRestart: 重启后，新实例创建后，未替换旧实例，super 会调用 preStart")
      super.postRestart(reason)
    }

    override def receive: Receive = { case e: Exception =>
      throw e
    }
  }
}
