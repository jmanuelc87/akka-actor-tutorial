package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbeSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val masterActor = system.actorOf(Props[MasterActor])
      val slave = TestProbe("slave")

      masterActor ! Register(slave.ref)

      expectMsg(Registration)
    }

    "send the work to a slave actor" in {
      val masterActor = system.actorOf(Props[MasterActor])
      val slave = TestProbe("slave")
      masterActor ! Register(slave.ref)
      expectMsg(Registration)

      val workLoadString = "I love Akka"

      masterActor ! Work(workLoadString)

      slave.expectMsg(SlaveWork(workLoadString, testActor))
      slave.reply(WorkComplete(3, testActor))

      expectMsg(Report(3))
    }

    "aggregate data correctly" in {
      val masterActor = system.actorOf(Props[MasterActor])
      val slave = TestProbe("slave")
      masterActor ! Register(slave.ref)
      expectMsg(Registration)

      val workLoadString = "I love Akka"

      masterActor ! Work(workLoadString)
      masterActor ! Work(workLoadString)

      slave.receiveWhile() {
        case SlaveWork(`workLoadString`, `testActor`) =>
          slave.reply(WorkComplete(3, testActor))
      }

      expectMsgAllOf(Report(3), Report(6))
    }
  }
}


object TestProbeSpec {

  case class Work(text: String)
  case class SlaveWork(text: String, originalRequest: ActorRef)
  case class WorkComplete(count: Int, originalRequester: ActorRef)
  case class Register(slaveRef: ActorRef)
  case object Registration
  case class Report(totalCount: Int)

  class MasterActor extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! Registration
        context.become(online(slaveRef, 0))
      case _ =>
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkComplete(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }
}
