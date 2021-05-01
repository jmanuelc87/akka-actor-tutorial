package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class InterceptingLogSpec extends TestKit(ActorSystem("BasicSpec", ConfigFactory.load().getConfig("interceptingLogMessages"))) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import InterceptingLogSpec._

  val item = "Rock"
  val creditCard = "1111"

  "A checkout flow" should {
    "correctly log the dispatch of the flow" in {
      EventFilter.info(pattern = s"Order for [0-9]+ for item $item has been dispatched", occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }

    "freaksout if the payment is denied" in {
      EventFilter[RuntimeException](occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, "0000")
      }
    }
  }
}

object InterceptingLogSpec {

  case class Checkout(item: String, creditCard: String)

  case class AuthorizeCard(creditCard: String)

  case object PaymentAccepted

  case object PaymentDenied

  case class DispatchOrder(str: String)

  case object OrderConfirmed

  class CheckoutActor extends Actor {

    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfilmentManager = context.actorOf(Props[FulfilmentManager])

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, creditCard) =>
        paymentManager ! AuthorizeCard(creditCard)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfilmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      case PaymentDenied =>
        throw new RuntimeException("I can't handle this")
    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirmed =>
        context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(creditCard) =>
        if (creditCard.startsWith("0")) sender() ! PaymentDenied
        else {
          Thread.sleep(5000)
          sender() ! PaymentAccepted
        }
    }
  }

  class FulfilmentManager extends Actor with ActorLogging {
    var orderId = 0

    override def receive: Receive = {
      case DispatchOrder(item) =>
        orderId += 1
        log.info(s"Order for $orderId for item $item has been dispatched")
        sender() ! OrderConfirmed
    }
  }

}
