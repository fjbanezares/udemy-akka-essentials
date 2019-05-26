package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App {

  class SimpleActorWithExplicitLogger extends Actor {
    // #1 - explicit logging
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      /*
        1 - DEBUG  verbose find out exactly what happens
        2 - INFO   careful, benign
        3 - WARNING/WARN  hott
        4 - ERROR  die
       */
      case message => logger.warning(message.toString)// LOG it
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging a simple message"

  // #2 - ActorLogging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two things: {} and {}", a, b) // Two things: 2 and 3
      case message => log.info(message.toString)
    }
  }

  val simplerActor = system.actorOf(Props[ActorWithLogging])
  simplerActor ! "Logging a simple message by extending a trait"

  simplerActor ! (42, 65)

}
