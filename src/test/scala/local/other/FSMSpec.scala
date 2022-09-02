/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.other

import akka.actor.{Actor, ActorLogging, LoggingFSM, Props}
import local.LocalSpec
import scala.concurrent.duration.DurationInt

/**
  * FSMSpec
  *
  * 当一个对象有多种状态，并且在不同状态下对外界的处理方式不同时，可以使用FSM来描述，这样复杂性会降低
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/15 15:33
  */
class FSMSpec extends LocalSpec {
  // language=HOCON
  override val config =
    """
      |akka.loglevel = DEBUG
      |akka.actor.debug.fsm = on
      |""".stripMargin

  "sample fsm" in {
    val machine = system.actorOf(Props[FSMSpec.Machine], "machine")
    machine ! FSMSpec.Machine.PowerOn
    (1 to 10).foreach { _ =>
      machine ! FSMSpec.Machine.DoSomething(2)
      Thread.sleep(50)
    }

    expectNoMessage()
  }
}

object FSMSpec {
  object Machine {
    // 定义状态机存储的数据 Data
    type Power = Int
    // 定义状态机的状态类型 Stat
    sealed trait Stat
    case object Starting extends Stat
    case object Running  extends Stat
    case object Stopping extends Stat
    case object Stopped  extends Stat

    // 定义状态机处理的 Event
    sealed trait EventMessage
    // user
    case object PowerOn                extends EventMessage
    case object PowerOff               extends EventMessage
    case class DoSomething(power: Int) extends EventMessage // 消耗能量
    // auto 只是为了演示有些状态变化需要时间
    case object GotoRunning extends EventMessage
    case object GotoStopped extends EventMessage
  }

  class Machine extends Actor with ActorLogging with LoggingFSM[Machine.Stat, Machine.Power] {
    import Machine._
    import context.dispatcher

    val scheduler = context.system.scheduler

    // 初始状态
    startWith(Stopped, 10)
    // 定义处于某种状态下，能够处理的事情，
    // 对于处在某种状态下不能处理消息时，会打印日志，不做任何处理，
    // 也可以自定义 whenUnhandled
    when(Stopped) { case Event(PowerOn, _) => goto(Starting) }
    when(Starting) { case Event(GotoRunning, _) => goto(Running) }
    when(Running) {
      case Event(DoSomething(p), _) =>
        val tmp = stateData - p
        if (tmp <= 0) goto(Stopping) else stay using tmp
      case Event(PowerOff, _) =>
        goto(Stopping)
    }
    when(Stopping) { case Event(GotoStopped, _) => goto(Stopped) }
    whenUnhandled { case Event(e, _) =>
      log.warning(s"machine 正在 $stateName, 无法进行 $e")
      stay()
    }

    // 状态变化时执行的一些操作
    onTransition {
      case Stopped -> Starting =>
        log.debug("the machine is starting, need 50ms")
        scheduler.scheduleOnce(50.millis, self, GotoRunning)

      case Starting -> Running =>
        log.debug("the machine is running")

      case Running -> Stopping =>
        log.debug("the machine is stopping, need 100ms")
        scheduler.scheduleOnce(100.millis, self, GotoStopped)

      case Stopping -> Stopped =>
        log.debug("the machine is stopped")
    }
  }

}
