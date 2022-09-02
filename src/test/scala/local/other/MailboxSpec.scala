/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.other
import local.LocalSpec
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch._
import com.typesafe.config.Config

/**
  * MailboxSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/15 15:22
  */
class MailboxSpec extends LocalSpec {
  // language=HOCON
  override val config =
    """
      |my-box {
      |  mailbox-type = "me.ooon.akkaia.local.other.MailboxSpec$MyPriorityMailbox"
      |}
      |akka.actor.deployment {
      |  /echo2 {
      |    mailbox= my-box
      |  }
      |}
      |my-dispatcher {
      |  mailbox-type = "akka.dispatch.UnboundedControlAwareMailbox"
      |}
      |""".stripMargin

  "编程的形式指定邮箱" in {
    val echo = system.actorOf(Props[MailboxSpec.Echo], "echo")
    echo ! "a"
    expectMsg("a")
  }

  "权重邮箱" in {
    val echo = system.actorOf(Props[MailboxSpec.Echo2], "echo2")
    val seq  = Seq("low", "low", "high", "high", "hi", "low")
    seq.foreach(i => echo ! i)
    expectMsgAllOf(seq: _*)

  }

  "创建时指定邮箱" in {
    val echo = system.actorOf(Props[MailboxSpec.Echo2].withMailbox("my-box"), "echo3")
    val seq  = Seq("low", "low", "high", "high", "hi")
    seq.foreach(i => echo ! i)
    expectMsgAllOf(seq: _*)
  }
  "ControlAwareMailbox 并且通过 withDispatcher 指定邮箱" in {
    case object MyControlMessage extends ControlMessage
    val echo = system.actorOf(Props[MailboxSpec.Echo2].withDispatcher("my-dispatcher"), "echo4")
    echo ! "1"
    echo ! "2"
    echo ! MyControlMessage
  }
}

object MailboxSpec {
  class Echo extends Actor with ActorLogging with RequiresMessageQueue[BoundedMessageQueueSemantics] {
    override def receive = { case x: String =>
      log.info(x)
      sender() ! x
    }
  }

  class Echo2 extends Actor with ActorLogging {
    override def receive = { case x =>
      log.info(x.toString)
      sender() ! x
    }
  }
  class MyPriorityMailbox(setting: ActorSystem.Settings, config: Config)
      extends UnboundedStablePriorityMailbox(PriorityGenerator {
        case "high" => 0
        case "low"  => 2
        case _      => 1
      })
}
