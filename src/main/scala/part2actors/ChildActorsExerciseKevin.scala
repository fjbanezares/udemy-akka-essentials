package part2actors

import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActorsExercise.WordCounterWorker

object ChildActorsExerciseKevin extends App {

  // Distributed Word counting

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCounterMaster extends Actor {

    import WordCounterMaster._

    override def receive: Receive = withoutChildren

    def withoutChildren: Receive = {
      case Initialize(nChildren) =>
        println("[master] initializing...")
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(workerRefs: Seq[ActorRef], a: Int, b: Int, status: Map[String, Int]): Receive = {
      case Initialize(nChildren) =>
        println("[master] initializing...")
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
      //context.become(withChildren(childrenRefs, 0, 0, Map()))
      case WordCountReply(id, longitud) =>  context.become(withChildren(workerRefs, 0, 0, status))
    }
  }
    class WordCounterworker extends Actor {

      import WordCounterMaster._

      override def receive: Receive = {
        case WordCountTask(id,texto) => sender() ! WordCountReply(id, texto.split(" ").length)
      }
    }
  /*
    create WordCounterMaster
    send Initialize(10) to wordCounterMaster
    send "Akka is awesome" to wordCounterMaster
      wcm will send a WordCountTask("...") to one of its children
        child replies with a WordCountReply(3) to the master
      master replies with 3 to the sender.

    requester -> wcm -> wcw
           r  <- wcm <-

   */
  // round robin logic
  // 1,2,3,4,5 and 7 tasks
  // 1,2,3,4,5,1,2
}
