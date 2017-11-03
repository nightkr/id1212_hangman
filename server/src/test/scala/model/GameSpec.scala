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

    "having lost the game" should {
      "not allow any more tries" in {
        val game = new Game("z")
        var letter = 'a'
        while (!game.gameOver) {
          game.tryLetter(letter)
          letter = (letter.toInt + 1).toChar
        }
        game.gameOver shouldEqual true
        game.isSolved shouldEqual false
        game.tryLetter('z')
        game.gameOver shouldEqual true
        game.isSolved shouldEqual false
      }
    }

    "having solved the game" should {
      "be won" in {
        val game = new Game("z")
        game.gameOver shouldEqual false
        game.isSolved shouldEqual false
        game.tryLetter('z')
        game.gameOver shouldEqual true
        game.isSolved shouldEqual true
      }
    }
  }
}
