package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing._
import com.typesafe.config.ConfigFactory

object RoutersApp extends App {

  /**
   * 1 - Manual router
   */
  class Master extends Actor {
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_${i}")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    private var router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      case message =>
        router.route(message, sender())

      case Terminated(ref) =>
        router = router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router = router.addRoutee(newSlave)

    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RoutersDemo", ConfigFactory.load().getConfig("routersDemo"))
  val master = system.actorOf(Props[Master])

  //  for (i <- 1 to 10) {
  //    master ! s"[${i}] Hello from the world"
  //  }

  /**
   * A router actor with its own children
   */

  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")

  //  for (i <- 1 to 10) {
  //    poolMaster ! s"[${i}] Hello from the world!"
  //  }


  /**
   * From Configuration
   */
  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")

  //  for (i <- 1 to 10) {
  //    poolMaster2 ! s"[${i}] Hello from the world!"
  //  }

  /**
   * Router with actors created elsewhere
   */
  val slaveList = (1 to 5).map(item => system.actorOf(Props[Slave], s"slave_$item")).toList

  val salvePaths = slaveList.map(ref => ref.path.toString)

  val groupMaster = system.actorOf(RoundRobinGroup(salvePaths).props())

  //  for (i <- 1 to 10) {
  //    groupMaster ! s"[${i}] Hello from the world!"
  //  }

  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")

  //  for (i <- 1 to 10) {
  //    groupMaster2 ! s"[${i}] Hello from the world!"
  //  }

  groupMaster2 ! Broadcast("Hello, everyone!")

  // PoisonPill and Kill are NOT routed

  system.terminate()
}
