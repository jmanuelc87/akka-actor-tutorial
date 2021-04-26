package org.oaksoft.akka.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.duration._

class BaseSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  // setup
  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A word count actor" should {
    "return the count of words in the message" in {
      val actor = system.actorOf(Props[WordCountActor])
      val message = "Hello world for now"
      actor ! message

      expectMsg(message)
      expectNoMessage(3 second)

      val reply = expectMsgType[String]
      assert(reply.length == 16)

      // one or the other
      expectMsgAnyOf("hi", "hello")

      // expect all this messages
      expectMsgAllOf("scala", "Akka")

      // receive a sequence of messages
      val messages = receiveN(2)

      assert(messages.length == 2)

      expectMsgPF() {
        case "Scala" => // do specific assertions
        case "Akka" =>
      }
    }
  }
}
