/*
 * Copyright (c) 2020-2023.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit <http://ooon.me> or mail to zhaihao@ooon.me
 */

package me.ooon.akkaia
package local.stream

import local.{LocalAsyncSpec, LocalSpec}

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.typesafe.scalalogging.StrictLogging

import java.nio.file.Paths
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

/**
  * AA_QuickStartSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2023-11-03 15:59
  */
class AA_QuickStartSpec extends LocalSpec with StrictLogging {

  "基础使用" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val source: Source[Int, NotUsed] = Source(1 to 100)
    val future: Future[Done]         = source.runForeach(i => logger.info(i.toString))
    future.onComplete(_ => system.terminate())
  }
}

class AA_QuickStartSpec2 extends LocalAsyncSpec with StrictLogging {
  "实际上返回的是future" in {
    Source(1 to 100)
      .runForeach(i => logger.info(i.toString))
      .map { _ ==> Done }
  }

  "文件 sink" in {
    val value:          Source[BigInt, NotUsed] = Source(1 to 100).scan(BigInt(1)) { (acc, next) => acc * next }
    val eventualResult: Future[IOResult]        = value.map(num => ByteString(s"$num\n")).runWith(FileIO.toPath(Paths.get("test.txt")))
    eventualResult.map { a =>
      logger.info(a.toString)
      a.|+
    }
  }

//  即使设置很块的流速，也不会内存溢出，因为有背压
  "组合流，控制流速" in {
    val source     = Source(1 to 100)
    val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
    val s          = System.currentTimeMillis()
    factorials
      .zipWith(Source(0 to 100)) { (num, idx) => s"$idx = $num" }
      .throttle(100, 1.second)
      .runForeach(i => logger.info(i))
      .map { r =>
        logger.info(s"cost ${System.currentTimeMillis() - s} ms")
        r ==> Done
      }
  }

  "filter map stream" in {
    val source:       Source[Int, NotUsed] = Source(1 to 20).throttle(5, 1.seconds)
    val filterSource: Source[Int, NotUsed] = source.filter(_ % 2 == 0).map(_ * 100)
    val eventualDone: Future[Done]         = filterSource.runWith(Sink.foreach[Int](i => logger.info(i.toString)))
    val eventualDone1 = filterSource.runForeach(i => logger.info(i.toString))
    eventualDone.map(_ ==> Done)
    eventualDone1.map(_ ==> Done)
  }

  "flatten stream" in {
    val source:        Source[Int, NotUsed] = Source(1 to 10)
    val flattenSource: Source[Int, NotUsed] = source.mapConcat(i => Array.fill(2)(i))
    val eventualDone:  Future[Done]         = flattenSource.runForeach(i => logger.info(i.toString))
    eventualDone.map(_ ==> Done)
  }

  "mat" in {
    val source:       Source[Int, NotUsed]       = Source(1 to 10).throttle(3, 1.second)
    val count:        Flow[Int, Int, NotUsed]    = Flow[Int].map(_ => 1)
    val sumSink:      Sink[Int, Future[Int]]     = Sink.fold(0)(_ + _)
    val counterGraph: RunnableGraph[Future[Int]] = source.via(count).toMat(sumSink)(Keep.right)
    val sum:          Future[Int]                = counterGraph.run()
    sum.map(_ ==> 10)
  }
}
