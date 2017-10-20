package esmonad

import cats.data.{EitherT, OptionT}
import cats.instances.future._
import org.scalatest._
import scala.concurrent.Future

class V5Spec extends AsyncFlatSpec with Matchers {

  "The V6 object" should "be valid" in {

    import V5App._

    val id = "123"
    (
      for {
        event1 <- EitherT.fromEither[Future](Turtle.create(id))
        _ <- EitherT.right(persist(event1))

        state1 <- OptionT(hydrate[Turtle]("123")).toRight("not found")
        event2 <- EitherT.fromEither(Turtle.look(state1, South))
        Some(state2) = handler(Some(state1), event2)
        event3 <- EitherT.fromEither(Turtle.forward(state2, 2))
        Some(state3) = handler(Some(state2), event3)
        event4 <- EitherT.fromEither(Turtle.look(state3, West))
        _ <- EitherT.right(persist(event2))
        _ <- EitherT.right(persist(event3))
        _ <- EitherT.right(persist(event4))

        state3 <- OptionT(hydrate[Turtle]("123")).toRight("not found")
      } yield state3
    ).value.map {
      _ shouldBe Right(Turtle("123", 0, -2, West))
    }

  }

}
