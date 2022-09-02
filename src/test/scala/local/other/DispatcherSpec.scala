/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.other

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.Future

/**
  * DispatcherSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/15 14:19
  */
class DispatcherSpec extends LocalSpec {
  // language=HOCON
  override val config =
    """
      |a1 {
      |  type = Dispatcher
      |  executor = "fork-join-executor"
      |  fork-join-executor {
      |    parallelism-min = 2 // 并行度最小值
      |    parallelism-factor = 2.0 // 这3个值决定最后的 并行度，可用的 processor * factor,但是必须在 min 和 max 之间
      |    parallelism-max = 10 // 并行度最大值
      |  }
      |  throughput = 100 // 一次线程切换一个 actor，最多处理这个actor 邮箱中的消息数量
      |}
      |akka.actor.deployment {
      |  /echo1 {
      |    dispatcher = a1
      |  }
      |}
      |""".stripMargin

  "编程方式指定 dispatcher" in {
    val echo = system.actorOf(Props[DispatcherSpec.Echo].withDispatcher("a1"), "echo")
    echo ! "hi"
    expectMsg("hi")
  }

  "配置文件指定" in {
    val echo = system.actorOf(Props[DispatcherSpec.Echo], "echo1")
    echo ! "hi"
    expectMsg("hi")
  }

  "给 future 使用" in {
    implicit val ec = system.dispatchers.lookup("a1")
    Future {
      1 + 1
    }.map { i =>
      logger.info(Thread.currentThread().getName)
      i ==> 2
    }
  }

}

object DispatcherSpec {
  class Echo extends Actor with ActorLogging {
    override def receive = { case x: String =>
      log.info(x)
      log.info(Thread.currentThread().getName)
      sender() ! x
    }
  }
}
