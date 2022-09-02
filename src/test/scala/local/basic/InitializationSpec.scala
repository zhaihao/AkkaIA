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
  * InitializationSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 00:47
  */
class InitializationSpec extends LocalSpec {

  "通过构造函数初始化" in {
    val dog = system.actorOf(Props[InitializationSpec.Dog], "dog")
    dog ! "hi"
    expectNoMessage()
  }

  "通过 preStart 函数初始化" in {
    val cat = system.actorOf(Props[InitializationSpec.Cat], "cat")
    cat ! "hi"
    expectNoMessage()
  }
}
object InitializationSpec {
  class Dog extends Actor with ActorLogging {
    log.info("init")

    override def receive = { case "hi" =>
      log.info("hi")
    }
  }
  class Cat extends Actor with ActorLogging {
    // preStart 在 actor 第一次创建时执行
    override def preStart() = {
      log.info("init")
    }

    override def receive = { case "hi" =>
      log.info("hi")
    }
  }

  class LittleCat extends Actor with ActorLogging {
    override def preStart() = {
      log.info("little cat will start")
    }
    override def receive = { case "hi" =>
      log.info("meow")
    }
  }

}
