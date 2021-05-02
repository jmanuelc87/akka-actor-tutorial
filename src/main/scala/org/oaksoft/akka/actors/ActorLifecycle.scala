package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object ActorLifecycle extends App {

  object StartChild

  class LifeCycleActor extends Actor with ActorLogging {

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info("I'm starting")

    override def postStop(): Unit = log.info("I have stopped")

    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifeCycleActor], "child")
    }
  }

  object Fail

  object FailChild

  object CheckChild

  object Check

  class ParentActor extends Actor with ActorLogging {

    private val child = context.actorOf(Props[ChildActor], "supervisedChild")

    override def receive: Receive = {
      case FailChild =>
        child ! Fail

      case CheckChild =>
        child ! Check
    }
  }

  class ChildActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("supervised child started")

    override def postStop(): Unit = log.info("supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting because of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("I failed")

      case Check =>
        log.info("Alive and kicking")
    }
  }

  val system = ActorSystem("LifeCycleDemo")
  //  val parent = system.actorOf(Props[LifeCycleActor], "parent")
  //
  //  parent ! StartChild
  //  parent ! PoisonPill


  val supervisor = system.actorOf(Props[ParentActor], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild
}
