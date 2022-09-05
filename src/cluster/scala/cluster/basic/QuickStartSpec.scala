/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package cluster.basic

import cluster.{ClusterSpec, Nodes}

import akka.actor.{Actor, ActorLogging, Props}
import QuickStartSpec._

/**
  * QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/16 13:49
  */
class QuickStartSpec extends ClusterSpec(nodes(2)) {

  def nodes(n:Int) = Nodes(n)

  "1" in {

    /**
     * runOn 不在乎 顺序
     * runOn 之外的代码运行在所有节点，需要注意顺序
     */
    runOn(roles.head) {
      enterBarrier("deployed")
      val pong = system.actorSelection(node(roles(1)) / "user" / "pong")
      logger.info(pong.toString())
      pong ! "ping"
      import scala.concurrent.duration._
      expectMsg(10.seconds, "pong")
    }

    runOn(roles(1)) {
      system.actorOf(Props[Pong], "pong")
      enterBarrier("deployed")
    }

    // 必须放在最后面，等待所有节点的 runOn 执行完毕
    enterBarrier("finished")

  }
}

object QuickStartSpec {
  //language=HOCON
  def nodes(nr:Int) = Nodes(nr).configAll(
    """
      |akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
      |""".stripMargin).debug(true)

  class Pong extends Actor with ActorLogging {

    def receive = { case "ping" =>
      log.info(sender().toString())
      sender() ! s"pong"
    }
  }
}

class QuickStartSpecJvm0 extends QuickStartSpec
class QuickStartSpecJvm1 extends QuickStartSpec
