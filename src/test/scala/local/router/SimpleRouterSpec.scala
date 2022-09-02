/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.router
import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props, Terminated}
import akka.routing._

/**
 * =SimpleRouterSpec=
 *
 * '''从表面上看，路由器就像普通的actor，但实际上它们的实现方式是不同的。路由器的设计是为了非常高效地接收消息，并将消息快速传递给路由者。
 * 普通的actor可以用于路由消息，但actor的单线程处理会成为瓶颈。路由器通过对普通的消息处理管道进行优化，允许并发路由，可以实现更高的吞吐量。
 * 这是通过将路由器的路由逻辑直接嵌入到它们的ActorRef中，而不是在路由器的actor中实现的。发送到路由器的ActorRef的消息可以立即被路由到routee，
 * 完全绕过单线程的actor。
 * 这样做的代价是，路由代码的内部结构比用普通的actor来实现路由器更加复杂。
 * 幸运的是，所有这些复杂性对于路由API的消费者来说都是看不见的。然而，当你实现自己的路由器时，这是需要注意的。'''
 *
 * '''如果您觉得Akka提供的路由器不能满足您的需求，您可以创建自己的路由器。为了推出您自己的路由器，您必须满足某些标准。'''
 *
 * '''在创建自己的路由器之前，你应该考虑一个具有类似路由器行为的普通角色是否可以像一个完整的路由器一样完成工作。
 * 如上所述，路由器比普通行为体的主要好处是它们的性能更高。但它们的编写比普通角色更复杂一些。
 * 因此，如果在你的应用中可以接受较低的最大吞吐量，你可能希望坚持使用传统的actor。'''
 *
 * @author zhaihao
 * @version 1.0
 * @since 2022/8/11 22:27
 */
class SimpleRouterSpec extends LocalSpec {
  import SimpleRouterSpec._
  // language=HOCON
  override val config =
    """|akka.actor.deployment {
       |  /router1 {
       |    router = round-robin-pool
       |    nr-of-instances = 3
       |  }
       |}
       |""".stripMargin

  "创建自己的router" in {
    val master = system.actorOf(Props[Master], "master")
    (1 to 6).foreach(i => master ! i)
    master ! "stop"
    master ! "stop"
    master ! "stop"
    // 可能会丢失部分消息，因为 worker 启动需要时间
    Thread.sleep(10)
    (1 to 10).foreach(i => master ! i)
  }

  "使用 akka 内置的 router" - {
    "读取配置文件" in {
      val router1 = system.actorOf(FromConfig.props(Props[Worker]), "router1")
      (1 to 6).foreach(i => router1 ! i)
      router1 ! "stop"
      router1 ! "stop"
      router1 ! "stop"
      //  actor 不会重启
      router1 ! 1
    }
    "编程形式" in {
      val router2 = system.actorOf(RoundRobinPool(3).props(Props[Worker]), "router2")
      (1 to 6).foreach(i => router2 ! i)
      router2 ! "stop"
      router2 ! "stop"
      router2 ! "stop"
      //  actor 不会重启
      router2 ! 1
    }
  }
}

object SimpleRouterSpec {
  class Master extends Actor with ActorLogging {
    var router = {
      val routees = Vector.fill(3) {
        val r = context.actorOf(Props[Worker])
        context.watch(r)
        ActorRefRoutee(r)
      }
      Router(RoundRobinRoutingLogic(), routees)
    }
    override def receive = {
      case Terminated(a) =>
        log.warning(s"remove $a")
        router = router.removeRoutee(a)
        val r = context.actorOf(Props[Worker])
        context.watch(r)
        router = router.addRoutee(r)
      case x => router.route(x, sender)
    }
  }

  class Worker extends Actor with ActorLogging {
    override def receive = {
      case x: Int =>
        log.info(x.toString)
        sender() ! x
      case "stop" => context.stop(self)
    }
  }
}
