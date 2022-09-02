/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.fault

import local.LocalSpec

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}

import scala.concurrent.duration.DurationInt

/**
  * =OneForOneStrategySpec=
  *
  * 这种策略只会处理抛出异常的 子actor
  *
  * 对子actor 的监管策略
  * maxNrOfRetries 表示在固定时间内重启的次数。withinTimeRange 表示多久重启次数会被重置
  *      1. '''(-1, Inf)''' 表示无任何限制
  *      1. '''(-1, NoInf)''' 表示这段时间只能重启一次 相当于 '''(1, NoInf)'''
  *      1. '''(NonNegative, Inf)''' 表示重启次数只能有这些次数
  *
  * 父 actor 重启时一定会尝试重启所有存活的 子 actor，如果 preRestart stop 了子 actor，则相当于 这个子actor 不再属于 父 actor
  *
  * '''注意：'''父 actor 重启时会重新执行构造函数，如果 preRestart 不stop 子 actor，则 子actor会变多
  *
  * 可以这样理解， actor 的stop 其实是销毁 actor 对象，actor 的start 是创建 actor对象，actor 的restart是销毁对象，创建对象，复用 ActoreRef
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 22:27
  */
//noinspection AppropriateActorConstructorNotFound
class OneForOneStrategySpec extends LocalSpec {

  "正常监管" in {
    val strategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 10.seconds, loggingEnabled = true) {
      case _: Exception => Restart
      case _: Error     => Stop
    }
    val cat = system.actorOf(Props(classOf[OneForOneStrategySpec.Cat], strategy), "cat")
    val e   = new Exception("crash")
    cat ! "hi"
    cat ! e
    cat ! "meow"
    expectNoMessage()
  }

  "重启超过次数限制" in {
    val strategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1.seconds, loggingEnabled = true) {
      case _: Exception => Restart
      case _: Error     => Stop
    }
    val cat = system.actorOf(Props(classOf[OneForOneStrategySpec.Cat], strategy), "cat1")
    val e   = new Exception("crash")
    cat ! "hi"
    cat ! e
    cat ! e
    cat ! e
    cat ! e
    cat ! "meow"
    expectNoMessage()
  }
  "重启次数重置" in {
    val strategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 3.millis, loggingEnabled = true) {
      case _: Exception => Restart
      case _: Error     => Stop
    }
    val cat = system.actorOf(Props(classOf[OneForOneStrategySpec.Cat], strategy), "cat2")
    val e   = new Exception("crash")
    cat ! "hi"
    cat ! e
    cat ! e
    cat ! e
    cat ! e
    cat ! "meow"
    expectNoMessage()
  }

  "不记录重启日志" in {
    val strategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 10.seconds, loggingEnabled = false) {
      case _: Exception => Restart
      case _: Error     => Stop
    }
    val cat = system.actorOf(Props(classOf[OneForOneStrategySpec.Cat], strategy), "cat4")
    val e   = new Exception("crash")
    cat ! "hi"
    cat ! e
    cat ! "meow"
    expectNoMessage()
  }

  "只有一个子 actor 重启" in {
    val strategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 10.seconds, loggingEnabled = true) {
      case _: NullPointerException => Restart
      case _: Exception            => Escalate
    }
    val cat = system.actorOf(Props(classOf[OneForOneStrategySpec.Cat], strategy), "cat5")
    cat ! new NullPointerException("let it restart")
  }

  val supervisor = system.actorOf(Props[OneForOneStrategySpec.Supervisor], "supervisor")
  // resume 表示 actor 忽略错误，内部状态得以保持
  "resume" in {
    supervisor ! Props[OneForOneStrategySpec.Child]
    val child = expectMsgType[ActorRef]
    child ! 42
    child ! "get"
    expectMsg(42)
    child ! new ArithmeticException("let it resume")
    child ! "get"
    expectMsg(42)
  }
  // restart 表示 actor 将会重启，内部状态丢失
  "restart" in {
    supervisor ! Props[OneForOneStrategySpec.Child]
    val child = expectMsgType[ActorRef]
    child ! 42
    watch(child)
    child ! new NullPointerException("let it restart")
    // 重启不会发出 Terminated
    // expectTerminated(child)
    child ! "get"
    expectMsg(0)
  }
  // 直接停止 actor
  "stop" in {
    supervisor ! Props[OneForOneStrategySpec.Child]
    val child = expectMsgType[ActorRef]
    watch(child)
    child ! new IllegalArgumentException("let it stop")
    expectTerminated(child)
  }

  /**
   *  上报给 监管者，监管者自身抛出这个异常，会导致监管者按照他自己的监管策略执行，
   *  默认是 restart [[SupervisorStrategy.defaultDecider]]
   */
  "escalate" in {
    supervisor ! Props[OneForOneStrategySpec.Child]
    val child = expectMsgType[ActorRef]
    watch(child)
    child ! "get"
    expectMsg(0)
    child ! new Exception("lit it Escalate")
    expectMsgType[Terminated]
  }
}

object OneForOneStrategySpec {

  // cat and his child
  class Cat(strategy: SupervisorStrategy) extends Actor with ActorLogging {

    override def preStart() = log.info("cat pre start")
    override def postStop() = log.info("cat post stop")
    override def postRestart(reason: Throwable) = {
      log.info("cat post restart")
      super.postRestart(reason)
    }
    override def preRestart(reason: Throwable, message: Option[Any]) = {
      log.info("cat pre restart")
      super.preRestart(reason, message)
    }

    override def supervisorStrategy = strategy
    val child1 = context.actorOf(Props[LittleCat])
    val child2 = context.actorOf(Props[LittleCat])

    override def receive = { case a =>
      child1 ! a
    }
  }

  class LittleCat extends Actor with ActorLogging {

    override def preStart() = log.info("little cat pre start")
    override def postStop() = log.info("little cat post stop")
    override def postRestart(reason: Throwable) = {
      log.info("little cat post restart")
      super.postRestart(reason)
    }
    override def preRestart(reason: Throwable, message: Option[Any]) = {
      log.info("little cat pre restart")
      super.preRestart(reason, message)
    }
    override def receive = {
      case e: Exception => throw e
      case s: String    => log.info(s)
    }
  }

  class Supervisor extends Actor with ActorLogging {

    override def preStart() = log.info("supervisor pre start")
    override def postStop() = log.info("supervisor post stop")
    override def preRestart(reason: Throwable, message: Option[Any]) = {
      log.info("supervisor pre restart")
      super.preRestart(reason, message)
    }
    override def postRestart(reason: Throwable) = {
      log.info("supervisor post restart")
      super.postRestart(reason)
    }

    override def supervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds, loggingEnabled = true) {
        case _: ArithmeticException      => Resume
        case _: NullPointerException     => Restart
        case _: IllegalArgumentException => Stop
        case _: Exception                => Escalate
      }
    override def receive = { case p: Props =>
      sender() ! context.actorOf(p)
    }
  }
  class Child extends Actor with ActorLogging {

    override def preStart() = log.info("child pre start")
    override def postStop() = log.info("child post stop")
    override def preRestart(reason: Throwable, message: Option[Any]) = {
      log.info("child pre restart")
      super.preRestart(reason, message)
    }
    override def postRestart(reason: Throwable) = {
      log.info("child post restart")
      super.postRestart(reason)
    }

    var state = 0
    override def receive = {
      case e: Exception => throw e
      case x: Int       => state = x
      case "get" => sender ! state
    }
  }
}
