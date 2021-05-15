package org.oaksoft.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {

  val system = ActorSystem("MailboxDemo", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  // cutom priority mailbox
  class SupportTicketPriorityMailbox(setting: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator {
        case message: String if message.startsWith("[PO]") => 0
        case message: String if message.startsWith("[P1]") => 1
        case message: String if message.startsWith("[P2]") => 2
        case message: String if message.startsWith("[P3]") => 3
        case _ => 4
      }
    ) {}


  //  val supportTicketActor = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  //  supportTicketActor ! "[P3] something 1"
  //  //  supportTicketActor ! PoisonPill
  //  supportTicketActor ! "[P0] something 2"
  //  supportTicketActor ! "[P2] something 3"
  //  supportTicketActor ! "[P0] something 4"
  //  supportTicketActor ! "[P3] something 5"
  //  supportTicketActor ! "[P1] something 6"


  // control-aware mailbox
  // we'll use UnboundedControlAwareMailbox

  case object ManagementTicket extends ControlMessage


//  val controlAwareActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
//  controlAwareActor ! "[P0] something 2"
//  controlAwareActor ! "[P2] something 3"
//  controlAwareActor ! "[P0] something 4"
//  controlAwareActor ! "[P3] something 5"
//  controlAwareActor ! "[P1] something 6"
//  controlAwareActor ! ManagementTicket


  val altControlAwareActor = system.actorOf(Props[SimpleActor], "altControlAwareActor")
  altControlAwareActor ! "[P0] something 2"
  altControlAwareActor ! "[P2] something 3"
  altControlAwareActor ! "[P0] something 4"
  altControlAwareActor ! "[P3] something 5"
  altControlAwareActor ! "[P1] something 6"
  altControlAwareActor ! ManagementTicket
}
