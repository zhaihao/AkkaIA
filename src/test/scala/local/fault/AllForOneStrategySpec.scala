/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.fault

import local.LocalSpec

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, AllForOneStrategy, Props, SupervisorStrategy}

import scala.concurrent.duration.DurationInt

/**
  * AllForOneStrategySpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/11 21:49
  */
//noinspection AppropriateActorConstructorNotFound
class AllForOneStrategySpec extends LocalSpec {

  "一个子actor的出现异常会导致父级对所有子节点使用策略" in {
    val strategy = AllForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 10.seconds, loggingEnabled = true) {
      case _: NullPointerException => Restart
      case _: Exception            => Escalate
    }
    val cat = system.actorOf(Props(classOf[AllForOneStrategySpec.Cat], strategy), "cat5")
    cat ! new NullPointerException("let it restart")
  }
}

object AllForOneStrategySpec {
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
}
