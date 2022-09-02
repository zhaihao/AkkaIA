/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.router
import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{DefaultResizer, RoundRobinPool}

import scala.concurrent.duration.DurationInt

/**
  * RouterResizeSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 23:09
  */
class RouterResizeSpec extends LocalSpec {

  "test" in {
    val router1 = system.actorOf(
      RoundRobinPool(
        nrOfInstances = 1, // 初始数量
        resizer = Some(
          DefaultResizer(
            lowerBound = 1,         // 最小个数
            upperBound = 4,         // 最大个数
            pressureThreshold = 0,  // 如何评判 routee 是否繁忙，0-routee 正在处理消息，1-routee 正在处理消息，并且它的邮箱有消息，n- routee的邮箱消息 > n
            rampupRate = 0.6,       // 每次增加routee的百分比(当前已有的 routee 数量)
            backoffThreshold = 0.3, // 减小 routee 的阈值，当繁忙的路由占比小于当前总数的30%时，触发减少操作。如果 <=0 ,则永远不执行 减少操作
            backoffRate = 0.1,      // 每次减少 routee 的百分比
            messagesPerResize = 1   // 每处理 n 条消息，触发一次 resize
          )
        )
      ).props(Props[RouterResizeSpec.Cat]),
      "router1"
    )

    // 不要并行发，本地 cpu 可能会被 actor 抢占
    // 可以发现刚开始 有4个 actor，后面变成1个
    (1 to 100).foreach(i => router1 ! i)
    expectNoMessage(1.seconds)
  }
}

object RouterResizeSpec {
  class Cat extends Actor with ActorLogging {
    override def receive = {
      case "stop" => context.stop(self)
      case x: Int       => log.info(x.toString)
      case e: Exception => throw e
    }
  }
}
