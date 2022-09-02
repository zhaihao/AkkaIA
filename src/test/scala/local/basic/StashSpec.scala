/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props, Stash}

/**
 * =StashSpec=
 * unstash 的消息顺序与原始消息'''一致'''
 *
 * 调用stash()会将当前消息（actor最后收到的消息）添加到actor的stash中。
 * 它通常在处理actor的消息处理程序中的缺省情况时被调用，以储藏那些没有被其他情况处理的消息。
 * 储藏同一条消息两次是非法的；这样做会导致一个IllegalStateException被抛出。stash也可能是有界的，
 * 在这种情况下，调用stash()可能会导致容量违规，从而导致StashOverflowException。
 * stash的容量可以使用邮箱配置的stash-capacity设置（一个Int）来配置。
 * 调用unstashAll()会将stash中的消息 enqueues到actor的邮箱中，
 * 直到达到邮箱的容量（如果有的话）为止（注意，stash中的消息是预先添加到邮箱中的）。
 * 如果一个有边界的邮箱溢出，会抛出一个MessageQueueAppendFailedException。
 * stash在调用unstashAll()后保证是空的。
 * stash由scala.collection.immutable.Vector.Vector支持。
 * 因此，即使是非常多的消息也可以被储藏，而不会对性能产生重大影响。
 *
 *
 * 请注意，stash是短暂的actor状态的一部分，与邮箱不同。因此，它应该像其他具有相同属性的actor状态的部分一样被管理。
 * 然而，Stash特质的preRestart实现将调用unstashAll()。这意味着，在actor重启之前，它将把所有隐藏的消息转移回actor的邮箱。
 * 这样做的结果是，当actor重启时，任何被隐藏的消息都会被传递到actor的新化身。这通常是理想的行为。
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/10 23:37
  */
class StashSpec extends LocalSpec {
  "测试 stash" in {
    val db = system.actorOf(Props[StashSpec.DB], "db")
    db ! "a"
    db ! "b"
    db ! "c"
    db ! "open"
    db ! "a"
    db ! "b"
    db ! "c"
    db ! "close"
    db ! "c"
    expectMsg("a")
    expectMsg("b")
    expectMsg("c")
    expectMsgAllOf("a", "b", "c")
  }
}
object StashSpec {
  class DB extends Actor with ActorLogging with Stash {
    override def receive = {
      case "open" =>
        unstashAll()
        context.become({
                         case "close" =>
                           context.unbecome()
                         case msg =>
                           log.info(msg.toString)
                           sender() ! msg
                       },
                       false
        )
      case _ => stash()
    }
  }
}
