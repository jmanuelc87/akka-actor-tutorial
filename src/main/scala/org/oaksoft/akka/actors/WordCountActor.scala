package org.oaksoft.akka.actors

import akka.actor.Actor

class WordCountActor extends Actor {

  var totalWords = 0

  def receive: PartialFunction[Any, Unit] = {
    case message: String =>
      println(s"I have received: $message")
      totalWords += message.split(" ").length
    case _ => println("Sorry can't understand the message")
  }

}
