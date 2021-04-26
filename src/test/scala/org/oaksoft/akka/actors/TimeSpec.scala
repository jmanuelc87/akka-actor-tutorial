package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random

class TimeSpec extends TestKit(ActorSystem("TimeSpec", ConfigFactory.load().getConfig("specialAssertionsConfig")))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import TimeSpec._

  "A worker actor" should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "Should reply in a timely manner" in {
      within(500 millis, 1 second) {
        workerActor ! "Work"
        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a resonable cadence" in {
      within(1 second) {
        workerActor ! "WorkSequence"

        val results: Seq[Int] = receiveWhile[Int](max = 2 second, idle = 500 millis, messages = 10) {
          case WorkResult(result) => result
        }

        assert(results.sum > 5)
      }
    }

    "reply to a test probe in a timely manner" in {
      within(1 second) {
        val probe = TestProbe()
        probe.send(workerActor, "Work")
        probe.expectMsg(WorkResult(42))
      }
    }
  }

}

object TimeSpec {

  case class WorkResult(i: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "Work" =>
        Thread.sleep(500)
        sender() ! WorkResult(42)
      case "WorkSequence" =>
        val r = new Random()
        for (i <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }
}
