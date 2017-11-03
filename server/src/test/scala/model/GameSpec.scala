package se.nullable.kth.id1212.hangman.server.model

import org.scalatest.{ Matchers, WordSpec }


class GameSpec extends WordSpec with Matchers {
  "A Game" when {
    "trying the same invalid letter twice" should {
      "ignore the second attempt" in {
        val game = new Game("asdf")
        val initialTries = game.triesRemaining
        game.tryLetter('q')
        game.triesRemaining shouldEqual initialTries - 1
        game.tryLetter('q')
        game.triesRemaining shouldEqual initialTries - 1
        game.tryLetter('w')
        game.triesRemaining shouldEqual initialTries - 2
        game.tryLetter('q')
        game.triesRemaining shouldEqual initialTries - 2
      }
    }

    "trying a valid letter" should {
      "not count it as an attempt" in {
        val game = new Game("asdf")
        val initialTries = game.triesRemaining
        game.tryLetter('a')
        game.triesRemaining shouldEqual initialTries
      }

      "reveal the letter in the clue" in {
        val game = new Game("asa")
        game.clue shouldEqual "___"
        game.tryLetter('a')
        game.clue shouldEqual "a_a"
      }
    }
  }
}
