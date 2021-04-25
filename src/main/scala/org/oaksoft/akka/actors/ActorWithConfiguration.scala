package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object ActorWithConfiguration extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val configString =
    """
      | akka {
      |   loglevel = DEBUG
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)

  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A message to remember"

  system.terminate()


  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileSystem")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])

  defaultConfigActor ! "A message to remember"

  defaultConfigFileSystem.terminate()

  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfig", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])

  specialConfigActor ! "Remember me, I am special"

  specialConfigSystem.terminate()

  val separateConfig = ConfigFactory.load("conf/secret.conf")
  println(s"Separate config log level: ${separateConfig.getString("akka.loglevel")}")

  /**
   * Can also use json and properties files
   */
}