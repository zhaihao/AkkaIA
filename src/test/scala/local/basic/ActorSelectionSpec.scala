/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSelection, Identify, Props}

/**
  * ActorSelectionSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/7 23:28
  */
class ActorSelectionSpec extends LocalSpec {

  "select a actor" in {
    val em = system.actorOf(Props[ActorSelectionSpec.MActor], "em")
    em ! "m"
    expectMsg("m")
    logger.info(em.toString())

    /**
     *  1. 通过 actorSelection 来获得一个 已经存在的 actor
     *  1. 如果确信 actor 存在，可以直接发送消息
     *  1. 如果不确定，可以发送 Identify 消息，所有 actor 都能处理这种消息
     *    1. 如果 actor 存在，则会回复一个 ActorIdentity 消息，并且会返它的 ref
     *    1. 如果 actor 不存在，也会回复一个 ActorIdentity，只不过 ref 为 none
     */
    val maybeEM: ActorSelection = system.actorSelection("/user/em")
    maybeEM ! "m"
    expectMsg("m")
    maybeEM ! Identify(1)
    expectMsgPF() { case ActorIdentity(id, ref) =>
      logger.info(s"$id: $ref")
      assert(ref.isDefined)
    }

    val maybeEM2: ActorSelection = system.actorSelection("/user/emm")
    maybeEM2 ! Identify(2)
    expectMsgPF() { case ActorIdentity(id, ref) =>
      logger.info(s"$id: $ref")
      assert(ref.isEmpty)
    }

  }

}

object ActorSelectionSpec {
  class MActor extends Actor with ActorLogging {
    override def receive = { case "m" =>
      sender() ! "m"
    }
  }

}
