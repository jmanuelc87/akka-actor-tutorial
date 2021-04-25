package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.oaksoft.akka.actors.ParentActor.{CreateChild, TellChild}

object ParentActor {

  case class CreateChild(name: String)

  case class TellChild(message: String)

}

class ParentActor extends Actor {

  import ParentActor._

  var child: ActorRef = _

  override def receive: Receive = {
    case CreateChild(name) =>
      println(s"${self.path} creating a child")
      val childRef = context.actorOf(Props[ChildActor], name)
      context.become(withChild(childRef))
  }

  def withChild(childRef: ActorRef): Receive = {
    case TellChild(message) => childRef forward message
  }
}

class ChildActor extends Actor {

  override def receive: Receive = {
    case message => println(s"${self.path} I got a Message: $message")
  }
}

object ParentChildDemo extends App {
  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[ParentActor], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("This is a message")
  system.terminate()

  val selection = system.actorSelection("/user/parent/child")
  selection ! "I found you!"
}

/**
 * Round Robin Loigc
 * 1 to 7 tasks
 * 1 to 5 children
 * children distribution
 * tasks: 1 2 3 4 5 6 7
 * child: 1 2 3 4 5 1 2
 */
