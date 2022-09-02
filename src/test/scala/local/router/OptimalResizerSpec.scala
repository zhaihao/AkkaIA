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
import akka.routing.FromConfig

/**
  * =OptimalResizerSpec=
  *
  * 通过跟踪每个池大小下的消息吞吐量，并定期执行以下三个调整大小的操作（一次一个）来实现。
  * 	- 如果在一段时间内没有看到所有的路由被充分利用，就缩小规模。
  * 	- 探索到附近的随机池大小，尝试收集吞吐量指标。
  * 	- 优化到一个附近的池子大小，有一个更好的（比其他任何附近大小）吞吐量指标。
  *
  * 当池子被充分利用时（即所有路由都很忙），它会随机选择探索和优化。当池子有一段时间没有被充分利用时，它将把池子缩小到最后看到的最大利用率乘以一个可配置的比例。
  * 通过不断的探索和优化，大小调整器最终会走到最佳大小并保持在附近。当最佳尺寸发生变化时，它将开始走向新的尺寸。
  * 它保留了一个性能日志，所以它是有状态的，同时比默认的Resizer有更大的内存占用。内存使用量是O(n)，其中n是你允许的尺寸数，即 upperBound - lowerBound。
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 23:35
  */
class OptimalResizerSpec extends LocalSpec {
  // language=HOCON
  override val config =
    """|akka.actor.deployment {
       |  /optimal-router {
       |    router = round-robin-pool
       |    optimal-size-exploring-resizer {
       |      enable = on
       |      lower-bound = 1
       |      upper-bound = 4
       |      action-interval = 1s
       |      downsize-after-underutilized-for = 72h
       |    }
       |  }
       |}
       |""".stripMargin

  // 普通的 resizer 智能根据固定的规则进行 resize 操作
  // optimal-size-exploring-resizer 则是根据历史的 吞吐量记录，来进行 resize
  // 下面的测试只是演示如何使用
  "更加智能的 resizer" in {
    val router = system.actorOf(FromConfig.props(Props[OptimalResizerSpec.Cat]), "optimal-router")
    (1 to 10).foreach(i => router ! i)
    expectNoMessage()
  }
}

object OptimalResizerSpec {
  class Cat extends Actor with ActorLogging {
    override def receive = {
      case "stop" => context.stop(self)
      case e: Exception => throw e
      case x: Int       => log.info(x.toString)
    }
  }
}
