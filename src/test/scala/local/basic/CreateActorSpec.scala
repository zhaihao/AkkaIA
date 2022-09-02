/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.basic

import local.LocalSpec

import akka.actor.{Actor, ActorLogging, Props}

/**
  * CreateActorSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/7 02:34
  */
class CreateActorSpec extends LocalSpec {
  import CreateActorSpec._

  "通过 Props[T] 进行创建" in {
    system.actorOf(Props[CreateActorSpec.Ping1]) ! "ping"
    expectMsg("pong")
  }

  "通过 Props(classOf[T]) 进行创建" in {
    // noinspection AppropriateActorConstructorNotFound
    system.actorOf(Props(classOf[CreateActorSpec.Ping1])) ! "ping"
    expectMsg("pong")
  }

  /**
   * 这种方式不推荐，能使用但是不能在 另一个 actor 内部使用，否则会破坏actor封装
   *
   * Never pass an actor’s this reference into Props
   */
  "通过 Props(new T) 进行创建" in {
    // noinspection AppropriateActorConstructorNotFound
    system.actorOf(Props(new Ping1)) ! "ping"
    expectMsg("pong")
  }

  "actor 参数为 value class" in {
    val cat  = Cat("katy")
    val cat1 = system.actorOf(Ping2.props2(cat))
    cat1 ! "ping"
    val cat2 = system.actorOf(Ping2.props3(cat))
    cat2 ! "ping"
    receiveN(2)
  }

  "actor 不支持默认参数" in {
    // val props1 = Props(classOf[DefaultValueActor],3) // error

    // noinspection AppropriateActorConstructorNotFound
    val a = system.actorOf(Props(classOf[DefaultValueActor], 2, 3))
    a ! 1
    expectMsg(9)
  }

  "actor 最佳实践" in {
    // 给 actor 起一个有意义的名字
    val hello = system.actorOf(MyActor.props("hello"), "myActor")
    (1 to 15).foreach(i => hello ! MyActor.Greeting(s"$i"))
    expectMsg(MyActor.Goodbye)
  }
}

object CreateActorSpec {

  class Ping1 extends Actor with ActorLogging {
    override def receive = { case "ping" =>
      log.info("pong")
      sender() ! "pong"
    }
  }

  case class Cat(name: String) extends AnyVal

  class Ping2(cat: Cat) extends Actor with ActorLogging {
    override def receive = { case "ping" =>
      log.info(s"${cat.name} pong")
      sender() ! "pong"
    }
  }

  object Ping2 {

    // def props1(cat:Cat) = Props(classOf[Ping2],cat) // error

    // noinspection AppropriateActorConstructorNotFound
    def props2(cat: Cat) = Props(classOf[Ping2], cat.name)
    def props3(cat: Cat) = Props(new Ping2(cat)) // new 的方式不能用在另一个 actor 内部
  }

  class DefaultValueActor(a: Int, b: Int = 5) extends Actor {
    def receive = { case x: Int =>
      sender() ! ((a + x) * b)
    }
  }

  class MyActor(greetingMessage: String) extends Actor with ActorLogging {
    import MyActor._

    var number = 10

    override def receive = { case Greeting(from) =>
      if (number == 0) {
        sender() ! Goodbye
      } else {
        number -= 1
        log.info(s"$greetingMessage $from")
      }
    }
  }

  /**
   *  1. 伴生对象中提供 actor 可以处理的消息
   *  1. 伴生对象中提供 actor 的构造方法
   */
  object MyActor {
    case class Greeting(from: String)
    case object Goodbye

    def props(msg: String) = Props(new MyActor(msg))
  }
}
