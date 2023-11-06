/*
 * Copyright (c) 2020-2023.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.stream

import local.LocalAsyncSpec

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Succeeded

import scala.annotation.unused
import scala.concurrent.Future

/**
  * AB_SourceSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2023-11-06 11:10
  */
class AB_SourceSpec extends LocalAsyncSpec with StrictLogging {

  "create source" in {
    @unused
    val s1: Source[Int, NotUsed] = Source(List(1, 2, 3))
    @unused
    val s2: Source[String, NotUsed] = Source.future(Future.successful("Hello Stream"))
    @unused
    val s3: Source[Int, NotUsed] = Source.single(1)
    @unused
    val s4: Source[Int, NotUsed] = Source.empty[Int]
    @unused
    val s5: Source[Int, NotUsed] = Source.repeat(1)
    Future(Succeeded)
  }
}
