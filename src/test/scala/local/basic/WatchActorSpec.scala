/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, Props, Terminated}

/**
  * WatchActorSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 21:04
  */
class WatchActorSpec extends LocalSpec {

  "watch 一个 actor" in {
    val hello = system.actorOf(Props[WatchActorSpec.WatchActor], "hello")
    hello ! "kill"
    expectMsg("finished")
  }
}
object WatchActorSpec {
  class WatchActor extends Actor {
    val child = context.actorOf(Props.empty, "child")

    /**
     * 当 watch 的 actor stop 时，会收到一个 Terminated 消息
     */
    context.watch(child)
    var lastSender = context.system.deadLetters

    override def receive = {
      case "kill"              => context.stop(child); lastSender = sender()
      case Terminated(`child`) => lastSender ! "finished"
    }
  }
}
