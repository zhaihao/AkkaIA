/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props, Timers}

import scala.concurrent.duration.DurationInt

/**
  * =TimerSpec=
  *
  *  1. '''fixed delay''': 触发是发生在上一次完成后的 delay 时间之后
  *
  * 2. '''fixed rate''': 触发是发生上一次触发后的 delay 时间之后，不管上次执行有没有完成
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 21:09
  */
class TimerSpec extends LocalSpec {

  "timer" in {
    system.actorOf(Props[TimerSpec.MyActor], "my")
    expectNoMessage()
  }
}
object TimerSpec {
  object MyActor {
    private case object TickKey
    private case object FirstTick
    private case object Tick
  }

  class MyActor extends Actor with Timers with ActorLogging {
    import MyActor._
    // 相同的key只能启动一个，第二个会cancel 第一个
    timers.startSingleTimer(TickKey, FirstTick, 1.millis)

    def receive = {
      case FirstTick =>
        log.info("first tick")
        log.info(sender().toString())
        timers.startTimerWithFixedDelay(TickKey, Tick, 5.millis)
      case Tick =>
        log.info("tick")
    }
  }
}
