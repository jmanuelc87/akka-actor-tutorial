package org.oaksoft.akka

import akka.actor.{Actor, ActorSystem, Props}
import org.oaksoft.akka.actors.{ATMActor, WordCountActor}
import org.oaksoft.akka.messages.{AccountBalance, Deposit, Withdraw}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success}

object AkkaActorMain {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("HelloAkka")

    println(system.name)

    val wordCounterActor = system.actorOf(Props[WordCountActor], "wordCounter")
    val anotherWordCounterActor = system.actorOf(Props[WordCountActor], "anotherWordCounter")
    val person = system.actorOf(Person.props("Bob"))

    wordCounterActor ! "I'm learning akka  and it's pretty cool"
    anotherWordCounterActor ! "A different message"
    person ! "Hi"
    person ! 999
    person ! Message("some special message")

    createBalanceHistory(system)

    Thread.sleep(7500)

    val isTerminated = system.terminate()

    isTerminated.onComplete {
      case Failure(exception) => println(s"Error terminating the actor system $exception")
      case Success(value) => println("Success")
    }
  }

  def createBalanceHistory(system: ActorSystem): Unit = {
    val atm = system.actorOf(Props[ATMActor])
    val rand = new Random(System.currentTimeMillis())
    (0 to 10).foreach {
      _ =>
        Thread.sleep(17)
        if (rand.nextDouble() <= 0.5) {
          atm ! Deposit(1L, "12345", math.round(rand.nextDouble() * 100))
        } else {
          atm ! Withdraw(1L, "12345", math.round(rand.nextDouble() * 100))
        }
    }

    atm ! AccountBalance(1L, "12345")
  }
}

class Person(name: String) extends Actor {

  override def receive: Receive = {
    case "Hi" =>
      println(s"Hi! my name is $name")
    case number: Int =>
      println(s"I have received a number: $number")
    case Message(contents) =>
      println(s"I have received a special message: $contents")
    case _ =>
  }
}

object Person {
  def props(name: String): Props = Props(new Person(name))
}

case class Message(contents: String)