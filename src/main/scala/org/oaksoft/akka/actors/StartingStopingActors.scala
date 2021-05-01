package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}
import org.oaksoft.akka.actors.StartingStopingActors.Parent.{StartChild, Stop, StopChild}

object StartingStopingActors extends App {

  val system = ActorSystem("StopingActorDemo")

  object Parent {

    case class StartChild(name: String)

    case class StopChild(name: String)

    case object Stop

  }

  class Parent extends Actor with ActorLogging {
    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Staring the child $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child with the name $name")
        val child = children.get(name)
        child.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
      case message => log.info(message.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  class Watcher extends Actor with ActorLogging {

    import Parent._

    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Started and watching child $name")
        context.watch(child)

      case Terminated(ref) =>
        log.info(s"The reference that I'm watching $ref has stopped")
    }
  }

  import Parent._

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child1")
  val child = system.actorSelection("/user/parent/child1")
  child ! "hi kid!"

  parent ! StopChild("child1")
  for (_ <- 1 to 50) child ! "are you still there!"

  parent ! StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  child2 ! "hi, second child"
  parent ! Stop
  for (_ <- 1 to 10) parent ! "parent, are you still there?"
  for (_ <- 1 to 100) child2 ! "second kid, are you still there?"

  val looseActor = system.actorOf(Props[Child])
  looseActor ! "Hello"
  looseActor ! PoisonPill
  looseActor ! "are you there?"


  val looseActor2 = system.actorOf(Props[Child])
  looseActor2 ! "Hello"
  looseActor2 ! Kill
  looseActor2 ! "are you there?"

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watched")
  val watched = system.actorSelection("/user/watcher/watched")

  Thread.sleep(500)

  watched ! PoisonPill

  system.terminate()
}
