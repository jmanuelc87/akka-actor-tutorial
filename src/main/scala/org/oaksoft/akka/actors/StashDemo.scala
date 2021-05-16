package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  /**
   * ResourceActor
   *  - open => it can receive read/write requests to the resource
   *  - otherwise it will postpone all read/write requests until state is open
   *
   * ResourceActor is closed
   *  - open => switch to the open state
   *  - Read, Write messages are POSTPONED
   *
   * ResourceActor is open
   *  - Read, Write are handled
   *  - Close => switch to the closed state
   *
   * [Open, Read, Read, Write]
   *
   * [Read, Open, Write]
   *  - stash Read
   *    Stash: [Read]
   *  - open => switch to the open state
   *    Mailbox: [Read, Write]
   *  - read and write are handled
   */

  case object Open

  case object Close

  case object Read

  case class Write(data: String)


  class ResourceActor extends Actor with ActorLogging with Stash {

    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        unstashAll()
        context.become(open)

      case message =>
        log.info(s"Stashing $message I can't handle it in the closed state")
        stash()
    }

    def open: Receive = {
      case Read =>
        log.info(s"I've read the $innerData")

      case Write(data) =>
        log.info(s"I am writing $data")
        innerData = data

      case Close =>
        log.info("Closing Resource")
        unstashAll()
        context.become(closed)

      case message =>
        log.info(s"Stashing $message because I can't handle it in the open state")
        stash()
    }
  }

  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor])

  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Open
  resourceActor ! Write("I love to stash")
  resourceActor ! Close
  resourceActor ! Read
  
}
