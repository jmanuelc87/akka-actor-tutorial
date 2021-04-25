package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}


// explicit logging
class SimpleActorWithExplicitLogger extends Actor {

  val logger: LoggingAdapter = Logging(context.system, this)

  override def receive: Receive = {
    case message => logger.info(message.toString)
  }
}

class SimpleActorWithLogging extends Actor with ActorLogging {
  override def receive: Receive = {
    case (a, b) => log.info("Two parameters: {} and {}", a, b)
    case message => log.info(message.toString)
  }
}

object Main extends App {

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger], name = "simpleActor")
  val actorLogging = system.actorOf(Props[SimpleActorWithLogging], name = "loggingActor")

  actor ! "Logging a simple message"
  actorLogging ! "Logging a simple message"
  actorLogging ! ("Maria", "Daniela")

  system.terminate()
}
