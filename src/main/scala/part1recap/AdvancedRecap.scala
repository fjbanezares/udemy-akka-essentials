package part1recap

import scala.concurrent.Future
import scala.util.Try

object AdvancedRecap extends App {

  // partial functions
  // operate only in a subset of the given input domain
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }
//this equivalent partial function as pf is based in patern matching
  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  //

  //function is just an extension of partial function
  // d
  val function: (Int => Int) = partialFunction

  //donde pones una f puedes poenr una pf
  val modifiedList = List(1,2,3).map {
    case 1 => 42
    case _ => 0
  }
  println(modifiedList)

  // lifting
  // pf into a total f
  //
  val lifted = partialFunction.lift // total function Int => Option[Int]
  lifted(2) // Some(65) because pf is defined for 2
  lifted(5000) // None bc the original doesn´t return

  // orElse
  //  it adds and additional pf etending the original
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  pfChain(5) // 999 per partialFunction
  pfChain(60) // 9000 because I chained and additional
  println(Try(pfChain(457))) // throw a MatchError

  // type aliases
  // feature easy to understand
  type ReceiveFunction = PartialFunction[Any, Unit]

  //we use in AKKA this kind of aliases of complex types to make it pretty
  def receive: ReceiveFunction = {
    case 1 => println("hello")
    case _ => println("confused....")
  }

  receive(1)
  receive("pepito")
  // implicits

  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("timeout"))// extra parameter list omitted

  // implicit conversions

  // 1) implicit defs
  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)

  //We can call method greet in Peter as if it was a Person
  //Because the implicit conversion that it is in the air
  println ("Peter".greet)
  // fromStringToPerson("Peter").greet - automatically by the compiler

  // 2) implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark!")
  }

  "Lassie".bark   //only 1! primary constructor parameter in the implicit class!!!
  // new Dog("Lassie").bark - automatically done by the compiler

  // implicits are nice but confuse if we don´t know how to organize them
  // we will speak about organization and scopes in depth
  // organize
  // local scope
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1,2,3).sorted // List(3,2,1) because will take the implicit from the local scope

  // imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("hello, future")
  }

  // companion objects of the types included in the call
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  List(Person("Bob"), Person("Alice")).sorted
  // List(Person(Alice), Person(Bob))
}
