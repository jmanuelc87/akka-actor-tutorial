package org.component.akka

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object AkkaActorMain {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("DonutStoreActorSystem")

    val isTerminated = system.terminate()

    isTerminated.onComplete {
      case Failure(exception) => println(s"Error terminating the actor system $exception")
      case Success(value) => println("Success")
    }

    Thread.sleep(5000)
  }
}
