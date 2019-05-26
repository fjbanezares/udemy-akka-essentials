

val pepe: Map[String,Int] = Map("hijo" -> 7, "padre"-> 2)

pepe - "hijo" - "tomas"


val Foo: String = "foo"
val foo: String = "bar"

val pepito: String = "foo"
def fn(pepi:String): String ={
  val Foo: String = "foo"
  val foo: String = "bar"
  pepi matches {
    case `foo` => "bar"
    case Foo => "foo"
  }
}

// prints "foo" correctly