package se.nullable.kth.id1212.hangman.server.model.controller

import se.nullable.kth.id1212.hangman.proto.Packet
import se.nullable.kth.id1212.hangman.server.model.Game

class GameController(sendPacket: Packet => Unit, close: () => Unit) {
  private val word = "asdf"
  private val game = new Game(word)

  def handlePacket(packet: Packet): Unit = {
    packet match {
      case Packet.TryLetter(letter) =>
        tryLetter(letter)
      case pkt =>
        println(s"Invalid packet: $pkt")
    }
  }

  private def tryLetter(letter: Char): Unit = {
    game.tryLetter(letter)
    sendPacket(gameStatePacket)
    if (game.gameOver) {
      sendPacket(Packet.GameOver(game.isSolved))
      close()
    }
  }

  private def gameStatePacket = Packet.GameState(game.triesRemaining, game.triedLetters, game.clue)
}
