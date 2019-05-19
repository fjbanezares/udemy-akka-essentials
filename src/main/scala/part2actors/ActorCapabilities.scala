package part2actors

import java.time.temporal.TemporalAmount

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.Person.LiveTheLife

import scala.util.Try._
//import part2actors.ChangingActorBehavior.Counter.Increment

import scala.util.Try
//import part2actors.ActorCapabilities.BankAccount.Deposit
//import part2actors.ActorCapabilities.Person.LiveTheLife

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello, there!" // replying to a message
      case message: String => println(s"[$self] I have received $message by ${context.sender()} " )
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) =>
        self ! content  // cojo string content y me lo paso a mi mismo
      case SayHiTo(ref) => ref ! "Hi!" // alice is being passed as the sender
      case WirelessPhoneMessage(content, ref) => ref forward   (content + "s") // i keep the original sender of the WPM
      // With forward we pass the ref of whoever sent me the message
      // With tell ! we pass self
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  // in practice use case classes and case objects

  simpleActor ! 42 // who is the sender?!

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self === `this` in OOP

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi!" // reply to "me"

  // 5 - forwarding messages
  // D -> A -> B
  // forwarding = sending a message with the ORIGINAL sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob) // noSender.

  /**
    * Exercises
    *
    * 1. a Counter actor
    *   - Increment
    *   - Decrement
    *   - Print
    *
    * 2. a Bank account as an actor
    *   receives
    *   - Deposit an amount
    *   - Withdraw an amount
    *   - Statement
    *   replies with
    *   - Success
    *   - Failure
    *
    *   interact with some other kind of actor
    */

  case class Increment2(am: Int) // case Object if no arguments, just a COMMAND
  case class Decrement2(am: Int)
  case class Print2(st: String)
  class Counter2 extends Actor {
    var amount = 0
    override def receive: Receive = {

      case Increment2(q) => {println(s"[$self] I have received $q") ; amount+=q}
      case Decrement2(q) => amount-=q
      case Print2(_) => println(s"[$self] I have received $amount by ${context.sender()}")


    }

  }

  val counter2 = system.actorOf(Props[Counter2], "myCounter2")


  case class Deposit2(am: Int)
  case class Withdraw2(am: Int)
  case class Statement2(st: String)
  class BankAccount2 extends Actor {
    var amount = 0
    override def receive: Receive = {

      case Deposit2(q) => {amount += q
        sender ! Success("the money was deposited")}
      case Withdraw2(q) => {if (amount >= q) {
        amount -= q; context.sender() ! Success("the money was Wirtdrawn")
      } else context.sender ! Failure("the money was deposited")}
      case Statement2(_) => println(amount)


    }
  }

  val bankActor = system.actorOf(Props[BankAccount2], "bankActor")
  val counterActor = system.actorOf(Props[Counter2], "counterActor")

  bankActor ! (Deposit2(12), alice)
  bankActor ! (Statement2("perro"), alice)

  // DOMAIN of the counter, best practice, put objects in the Companion object of the actor class
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print


  // bank account
  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }

  class BankAccount extends Actor {
    import BankAccount._

    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! TransactionFailure("invalid deposit amount")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"successfully deposited $amount")
        }
      case Withdraw(amount) =>
        if (amount < 0) sender() ! TransactionFailure("invalid withdraw amount")
        else if (amount > funds) sender() ! TransactionFailure("insufficient funds")
        else {
          funds -= amount
          sender() ! TransactionSuccess(s"successfully withdrew $amount")
        }
      case Statement => sender() ! s"Your balance is $funds"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._
    import BankAccount._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(110000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println("acabo de recibir del banco: " + message.toString) // así lo vamos poniendo todo en la pantalla
    }
  }

  val account = system.actorOf(Props[BankAccount], "bankAccount")
  val person = system.actorOf(Props[Person], "billionaire")

  person ! LiveTheLife(account)

}

// Non blocking and async
// How can I guarantee that the messages are ordered...
// messages between 2 actors have order guarantee
// race conditions?
// Actor
// Akka has a thread pool 100s that can handle 1000000s of actors per MB of HEAP
// Actor is just a data structure
// needs a thread to process a message
// at certain point a thread will take control extracting msgs in order
// the thread invokes the handler on each message
// at some point the actor is unscheduled BUT after processing message that is ATOMIC
// only one thread operates an actor at a time, effectively single threaded
// no locks needed
// msg deliver guarantee
// at most once, no duplicates
// for any sender-receiver pair, the message order is maintained
// But can be intermingled with msgs from other actors


