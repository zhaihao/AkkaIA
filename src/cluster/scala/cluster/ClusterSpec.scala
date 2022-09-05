/*
 * Copyright (c) 2020-2022.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package cluster

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec, MultiNodeSpecCallbacks}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.BeforeAndAfterAll
import test.BaseSpecLike

/**
  * ClusterSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2022/8/16 13:50
  */
abstract class ClusterSpec(nodes: Nodes)
    extends MultiNodeSpec(nodes)
    with MultiNodeSpecCallbacks
    with BaseSpecLike
    with BeforeAndAfterAll
    with StrictLogging
    with ImplicitSender {
  self: TestKit =>

  override def initialParticipants = roles.length

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}
class Nodes private (nr: Int) extends MultiNodeConfig {
  private val ns = (0 until nr).map(i => role(s"node$i"))

  def configAll(config: String): this.type = {
    commonConfig(ConfigFactory.parseString(config))
    this
  }

  def debug(bool: Boolean): this.type = {
    debugConfig(bool)
    this
  }

  def configOne(f: Int => String): this.type = {
    (1 until nr).foreach { i =>
      nodeConfig(ns(i))(ConfigFactory.parseString(f(i)))
    }
    this
  }
}
object Nodes {
  def apply(nr: Int) = new Nodes(nr)
}
