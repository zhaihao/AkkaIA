/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, Props}

/**
  * SpecialActorSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 19:31
  */
class SpecialActorSpec extends LocalSpec {

  // 会产生一个死信
  "empty behavior" in {
    val a = system.actorOf(Props[SpecialActorSpec.AActor])
    a ! "1"
  }

  "ignore behavior" in {
    // 不会产生死信
    val b = system.actorOf(Props[SpecialActorSpec.BActor])
    b ! "1"
  }

  "test kit 中包含的特殊 actor" - {
    import akka.testkit.TestActors._
    "EchoActor" in {
      val echo = system.actorOf(Props[EchoActor], "echo")
      echo ! "hi"
      expectMsg("hi")
    }

    "BlackHoleActor" in {
      val blackHole = system.actorOf(Props[BlackholeActor], "blackHole")
      blackHole ! "hi"
      expectNoMessage()
    }

    "ForwardActor" in {
      val echo         = system.actorOf(Props[EchoActor], "echo1")
      val forwardActor = system.actorOf(Props(classOf[ForwardActor], echo), "forwardActor")
      forwardActor ! "hi"
      expectMsg("hi")
    }
  }
}
object SpecialActorSpec {
  class AActor extends Actor { override def receive = Actor.emptyBehavior    }
  class BActor extends Actor { override def receive = Actor.ignoringBehavior }
}
