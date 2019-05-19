package part1recap

import scala.util.Try

object GeneralRecap extends App {

  val aCondition: Boolean = false

  var aVariable = 42
  aVariable += 1 // aVariable = 43

  // expressions
  val aConditionedVal = if (aCondition) 42 else 65

  // code block delimited by curly braces, result is value of last expresion
  val aCodeBlock = {
    if (aCondition) 74
    56
  }

  // types
  // Unit, denotes side effects
  val theUnit = println("Hello, Scala")
  //it does sth that is a side effect but not produces anything
  // We use a lot Unit in AKKA context

  def aFunction(x: Int): Int = x + 1
  // recursion - TAIL recursion
  //parameters separated by a comma
  //Tail avoids stackoverflow for excessive recursion
  // We keep the result in an accoumulator
  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, acc * n)

  // OOP

  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog //Polimorphism

  trait Carnivore {
    def eat(a: Animal): Unit //trait may have methos implemented
  }

  class Crocodile extends Animal with Carnivore {
    //We have to implement whatever is abstract in both types
    override def eat(a: Animal): Unit = println("crunch!")
  }

  // method notations
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog //infix notation when only one argument

  // anonymous classes, good way
  // to implement classes that extend from abstract typeds
  // on the spot withour declaring special types for them

  /*
  * A normal class can implement any number of interfaces
  * but anonymous inner class can implement
  * only one interface at a time.
    A regular class can extend a class
    and implement any number of interface simultaneously.
    But anonymous Inner class can extend a class
    or can implement an interface but not both at a time.
    For regular/normal class, we can write any number of constructors
    but we cant write any constructor for anonymous Inner class
    because anonymous class does not have any name
    and while defining constructor class name
    and constructor name must be same.*/

  /*Like local classes, anonymous classes can capture variables; they
  have the same access to local variables of the enclosing scope:

  An anonymous class has access to the members of its enclosing class.
An anonymous class cannot access local variables
in its enclosing scope that
are not declared as final or effectively final.

Like a nested class, a declaration of a type (such as a variable) in an anonymous class shadows any other
declarations in the enclosing scope that have the same name.*/

  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar")
  }

  aCarnivore eat aDog

  // generics
  abstract class MyList[+A]
  // companion objects
  object MyList //This Singleton object is the companion of the abstract class


  // case classes
  case class Person(name: String, age: Int) // a LOT in this course!

  // Exceptions
  val aPotentialFailure = try {
    throw new RuntimeException("I'm innocent, I swear!") // Nothing  especial type returned
  } catch {
    case e: Exception => "I caught an exception!"
  } finally  {
    // side effects
    println("some logs")
  }

  // Functional programming

  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42) // 43
  // incrementer.apply(42)  //only if you define an apply method it does the trick

  val anonymousIncrementer = (x: Int) => x + 1
  // Int => Int === Function1[Int, Int]
  // Syntax sugar of the anonymous function

  // FP is all about working with functions as first-class
  List(1,2,3).map(incrementer)
  // map = HOF, Higuer Order Function that takes a f as parameter or gives one as result


  // for comprehensions
  val pairs = for {
    num <- List(1,2,3,4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char

  // List(1,2,3,4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "-" + char))
println(pairs)
  // Seq, Array, List, Vector, Map, Tuples, Sets

  // "collections"
  // Option and Try
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  println(aTry)

  // pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I don't know my name"
  }

  //s"Hi, my name is $n" interpreted string

  // ALL THE PATTERNS
  //  decompose collections, case classes

  // Some basic generics will  be needed

}
