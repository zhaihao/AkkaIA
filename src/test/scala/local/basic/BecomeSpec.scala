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
  * =BecomeSpec=
  * actor 用一个额外的空间保存 init behavior , 之后切换时使用 stack 记录behavior的变化。
  * 需要注意的是 '''become''' 有个参数 discardOld
  *
  *     - 默认为 true，效果是每次 unbecome 都会切换到 init behavior，这种情况下'''永远不会栈溢出'''。
  *     - 当这个参数为 false 时，将使用 stack 完整记录 behavior 的变化，这时要保证 become 与 unbecome 数一致，
  *       否则会导'''致栈溢出'''
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/10 23:40
  */
class BecomeSpec extends LocalSpec {
  val cat = system.actorOf(Props[BecomeSpec.Cat], "cat")

  "become/unbecome" in {
    (1 to 30).foreach(_ => cat ! "hi")
    expectNoMessage()
  }

  // 3 之后直接切到 1
  val dog = system.actorOf(Props[BecomeSpec.Dog], "dog")
  "become discardOld = true" in {
    (1 to 10).foreach(_ => dog ! 1)
    expectNoMessage()
  }

  // 3 之后切到 2
  val pig = system.actorOf(Props[BecomeSpec.Pig], "pig")
  "become discardOld = false" in {
    (1 to 10).foreach(_ => pig ! 1)
    expectNoMessage()
  }
}

object BecomeSpec {
  class Cat extends Actor with ActorLogging {
    var c = 0

    override def receive = happy

    def happy: Receive = { case "hi" =>
      c += 1
      log.info("meow")
      if (c > 4) {
        context.become(tired, discardOld = false)
        c = 0
      }
    }

    def tired: Receive = { case "hi" =>
      c += 1
      log.info("zzz")
      if (c > 2) {
        context.unbecome()
        c = 0
      }
    }
  }

  class Dog extends Actor with ActorLogging {
    override def receive = method1

    var c = 1

    def method1: Receive = { case _ =>
      log.info("behavior 1")
      c += 1
      if (c > 2) {
        context.become(method2)
        c = 1
      }
    }

    def method2: Receive = { case _ =>
      log.info("behavior 2")
      c += 1
      if (c > 3) {
        context.become(method3)
        c = 1
      }
    }

    def method3: Receive = { case _ =>
      log.info("behavior 3")
      context.unbecome()
    }
  }
  class Pig extends Actor with ActorLogging {
    override def receive = method1

    var c = 1

    def method1: Receive = { case _ =>
      log.info("behavior 1")
      c += 1
      if (c > 2) {
        context.become(method2, false)
        c = 1
      }
    }

    def method2: Receive = { case _ =>
      log.info("behavior 2")
      c += 1
      if (c > 3) {
        context.become(method3, false)
        c = 1
      }
    }

    def method3: Receive = { case _ =>
      log.info("behavior 3")
      context.unbecome()
    }
  }

}
