/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.fault

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}

import scala.concurrent.duration.DurationInt

/**
  * BackoffSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 22:31
  */
class BackoffSpec extends LocalSpec {

  /**
   * 这个例子并没有演示 backoff 的效果，只是演示如何配置
   * backoff 是指数退避算法
   * minBackoff 是第一次重启的 delay 时间，之后每重启一次 delay 时间都再上一次的基础上 乘以2
   * maxBackoff 最大delay 时间，当若干次重启后，delay 时间大于 maxBackoff 时，使用 maxBackoff 作为 delay 时间，而非继续 乘以2
   * randomFactor 为了防止多个子actor情况下一起重启，造成 启动压力过大，加入随机性避免这个问题，延迟时间为 delay * (1+ randomDouble * randomFactor)
   * 当达到 maxBackoff 后，如果不重置计数，则之后一直以 maxBackoff 的delay执行，如果需要重置，可以配置
   *  withAutoReset(time) 和 withManualReset，withAutoReset(time) 表示 如果actor启动成功，并在 time 时间内正常，则 backoff delay 重置为 minBackoff
   *  withManualReset 表示需要手工发送 reset 消息来进行将 delay 重置为 minBackoff
   *
   * onFailure 只有当 actor 不是被主动 stop 的情况下，才会重启。
   * onStop 无论 actor 出于什么原因都会被重启，如果确实不需要重启了，则需要配置 withFinalStopMessage，当需要停止时发送这个 final stop message
   *
   * 需要注意，这里的重启并使 restart，而是 stop actor ，start actor，所以 preStart 并不会执行
   *
   * 使用 Backoff 会产生一个监管者 actor
   * onStop 对应的监管者是 [[akka.pattern.internal.BackoffOnRestartSupervisor]]
   * onFailure 对应的监管者是 [[akka.pattern.internal.BackoffOnStopSupervisor]]
   *
   * withHandlerWhileStopped 默认情况下，当子actor stop 时，会转发一个消息到 死信队列，可以使用这个函数将这个消息发给自定义处理的 actor
   */
  "backoff 简单示例" in {
    val supervisor = BackoffSupervisor.props(
      BackoffOpts
        .onStop(childProps = Props[BackoffSpec.EchoActor], childName = "echo", minBackoff = 1.seconds, maxBackoff = 4.seconds, randomFactor = 0.2)
        .withFinalStopMessage(_ == "Stop")
    )

    val echoSupervisor = system.actorOf(supervisor, "echoSupervisor")
    echoSupervisor ! "hi"
    expectNoMessage()
  }

}

object BackoffSpec {
  class EchoActor extends Actor with ActorLogging {
    override def receive = { case x: String =>
      log.info(x)
    }
  }
}
