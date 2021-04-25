package org.oaksoft.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.oaksoft.akka.FussyKid.{KidAccept, KidReject}
import org.oaksoft.akka.Mom._
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

    val fuzzyKid = system.actorOf(Props[FussyKid], name = "fussyKid")
    val mom = system.actorOf(Props[Mom], name = "mom")
    val statelessFussyKid = system.actorOf(Props[StatelessFussyKid], name = "statelessFussyKid")

    mom ! MomStart(statelessFussyKid)

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

object FussyKid {

  case object KidAccept

  case object KidReject

  val HAPPY = "happy"

  val SAD = "sad"
}

class FussyKid extends Actor {

  import FussyKid._

  var state: String = HAPPY

  override def receive: Receive = {
    case Food(VEGTABLES) => state = SAD
    case Food(CHOCOLATE) => state = HAPPY
    case Ask(message) =>
      if (state == HAPPY) sender() ! KidAccept
      else sender() ! KidReject
  }
}

object Mom {

  case class MomStart(kidRef: ActorRef)

  case class Food(food: String)

  case class Ask(message: String)

  val VEGTABLES = "veggies"

  val CHOCOLATE = "chocolate"
}

class Mom extends Actor {
  override def receive: Receive = {
    case MomStart(kidRef) =>
      kidRef ! Food(VEGTABLES)
      kidRef ! Food(VEGTABLES)
      kidRef ! Food(CHOCOLATE)
      kidRef ! Food(CHOCOLATE)
      kidRef ! Ask("do you want to play")
    case KidAccept => println("Yay, my kid is happy!")
    case KidReject => println("My Kid is sad, but he's healthy!")
  }
}

class StatelessFussyKid extends Actor {

  import FussyKid._
  import Mom._

  override def receive: Receive = happyReceive

  def happyReceive: Receive = {
    case Food(VEGTABLES) => context.become(sadReceive, discardOld = true) // change my handler to sadReceive
    // with second params with false pushes to a stack the handler
    case Food(CHOCOLATE) => context.unbecome()
    case Ask(_) => sender() ! KidAccept
  }

  def sadReceive: Receive = {
    case Food(VEGTABLES) => context.unbecome()
    case Food(CHOCOLATE) => context.become(happyReceive, discardOld = false) // change my handler to happyReceive
    case Ask(_) => sender() ! KidReject
  }
}
