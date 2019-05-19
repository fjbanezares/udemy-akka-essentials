package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._   //importante de mirar en profundidad
import scala.util.Random

class  BasicSpec extends TestKit(ActorSystem("BasicSpec"))
//when we run we instantiate sn Actor System with name "BasicSpec" and then we will mix in some interfaces
  with ImplicitSender
  with WordSpecLike  //trait from ScalaTest allows the description of friends in a BDD, near natural language
  with BeforeAndAfterAll { //Some hooks for when you run the suite{

  // setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system) //destroy the test suite, "system is a member of TestKit
  }

  import BasicSpec._

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message

      //Async test in which we say expect "message" for a certain amount of time (e seconds default)... as defined in TestKit trait
      expectMsg(message) // time is configurable with akka.test.single-expect-default property
      // intertnally the time is that or the remainder...
      //Test Actor is implicitly who send the messages in the framework
      //Test Actor has a queue, Blocking Deque
    }
  }

  "A blackhole actor" should {
    "send back some message" in {
      val blackhole = system.actorOf(Props[Blackhole])
      val message = "hello, test"
      blackhole ! message

      expectNoMessage(1 second)

      //...expectNoMsg is deprecated ...
      //...scala concurrent duration is basics
      // test Actor is a Member of Test Kit and it is an actor used to communicate with test actor
      // he is the implicit sender of every msg
    }
  }

  // message assertions
  "A lab test actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor]) // creation of the actor

    "turn a string into uppercase" in {
      labTestActor ! "I love Akka"
      val reply = expectMsgType[String]

      assert(reply == "I LOVE AKKA")

      //pasa expectMsg("I LOVE AKKA") but here we obtain the message        val reply = expectMsgType[String]
      // then you can play around with the message...

    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")         //we can pass as many strings as we want
    }

    "reply with favorite tech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")            //smarter assertion, we expect all of the messages...
    }

    "reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2) // Seq[Any]            we just check we received 2 messages

      // free to do more complicated assertions
    }

    "reply with cool tech in a fancy way" in {
      labTestActor ! "favoriteTech"

      expectMsgPF() {
        case "Scala" => // only care that the PF is defined       (for the messages we care about..)
        case "Akka" =>
      }
    }

    //expectMsgPF     allows for more granular tests
    // eg we have case classes or more complex structures...    power
    // all other primitives can be implemented via expectMsgPF, very powerful
    // complex assertions at reception time...


  }


}

object BasicSpec { //implement all values we eill be using throughout the tests

  class SimpleActor extends Actor { //this is an echo actor
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class Blackhole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()

    override def receive: Receive = {
      case "greeting" =>
        if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()      //basic message to upper

    }
  }
}
