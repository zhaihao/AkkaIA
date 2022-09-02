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
  * =ExtendBehaviorSpec=
  *
  * @note '''如果 case 匹配有重叠，需要注意顺序'''
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 00:42
  */
class ExtendBehaviorSpec extends LocalSpec {

  "actor 的行为复用" in {
    val dog = system.actorOf(Props[ExtendBehaviorSpec.Dog], "dog")
    dog ! "hi"
    dog ! "woof"
    expectNoMessage()
  }
}

object ExtendBehaviorSpec {

  trait CommonBehavior { this: Actor with ActorLogging =>
    val sayHi: Receive = { case "hi" =>
      log.info("hi")
    }
  }

  class Dog extends Actor with ActorLogging with CommonBehavior {
    override def receive =
      sayHi.orElse({ case "woof" =>
        log.info("woof")
      })
  }

}
