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
  * =PoolVsGroupSpec=
  *   - 在Router-Pool模式中Router负责构建所有的Routee。如此所有Routee都是Router的直属子级Actor，
  *     可以实现Router对Routees的直接监管。由于这种直接的监管关系，Router-Pool又可以按运算负载自动增减Routee，能更有效地分配利用计算资源。
  *   - Router-Group模式中的Routees由外界其它Actor产生，特点是能实现灵活的Routee构建和监控，可以用不同的监管策略来管理一个Router下的Routees，
  *     比如可以使用BackoffSupervisor。从另一方面来讲，Router-Group的缺点是Routees的构建和管理复杂化了，而且往往需要人为干预。
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/12 23:59
  */
class PoolVsGroupSpec extends LocalSpec {
  // language=HOCON
  override val config =
    """|akka.actor.deployment {
       |  /router-pool {
       |    router = round-robin-pool
       |    nr-of-instances = 3
       |  }
       |  /router-group {
       |    router = round-robin-group
       |    routees.paths=["/user/cat1","/user/cat2","/user/cat3"]
       |  }
       |}
       |""".stripMargin

  "pool" in {
    val router = system.actorOf(FromConfig.props(Props[PoolVsGroupSpec.Cat]), "router-pool")
    (1 to 7).foreach(i => router ! i)
  }

  "group" in {
    system.actorOf(Props[PoolVsGroupSpec.Cat], "cat1")
    system.actorOf(Props[PoolVsGroupSpec.Cat], "cat2")
    system.actorOf(Props[PoolVsGroupSpec.Cat], "cat3")
    val router = system.actorOf(FromConfig.props(), "router-group")
    (1 to 7).foreach(i => router ! i)
  }
}

object PoolVsGroupSpec {
  class Cat extends Actor with ActorLogging {
    override def receive = { case x =>
      log.info(x.toString)
    }
  }
}
