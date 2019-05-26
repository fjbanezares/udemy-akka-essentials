package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  // Distributed Word counting

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)  //Here is the Magic of the Identification Number
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println("[master] initializing...")
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")  //creamos una lista de work count workers
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received: $text - I will send it to child $currentChildIndex")
        val originalSender = sender()  //the sender of the text
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)  // cogemos el ActorRef
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1  //empieza siendo 0
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task id $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))

        // map - key, quita ese elemento del Map
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love Akka", "Scala is super dope", "yes", "me too")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }

  val system = ActorSystem("roundRobinWordCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"


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

  /*

[master] initializing...
[master] I have received: I love Akka - I will send it to child 0
[master] I have received: Scala is super dope - I will send it to child 1
[master] I have received: yes - I will send it to child 2
[master] I have received: me too - I will send it to child 0
akka://roundRobinWordCountExercise/user/testActor/master/wcw_1 I have received task 0 with I love Akka
akka://roundRobinWordCountExercise/user/testActor/master/wcw_2 I have received task 1 with Scala is super dope
akka://roundRobinWordCountExercise/user/testActor/master/wcw_3 I have received task 2 with yes
[master] I have received a reply for task id 0 with 3
akka://roundRobinWordCountExercise/user/testActor/master/wcw_1 I have received task 3 with me too
[master] I have received a reply for task id 2 with 1
[test actor] I received a reply: 3
[master] I have received a reply for task id 1 with 4
[test actor] I received a reply: 1
[master] I have received a reply for task id 3 with 2
[test actor] I received a reply: 4
[test actor] I received a reply: 2
   */
}
