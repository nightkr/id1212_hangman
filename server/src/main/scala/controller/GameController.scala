package se.nullable.kth.id1212.hangman.server.model.controller

import org.slf4j.LoggerFactory
import se.nullable.kth.id1212.hangman.proto.Packet
import se.nullable.kth.id1212.hangman.server.model.Game

class GameController(sendPacket: Packet => Unit, close: () => Unit) {
  private val word = "asdf"
  private var game = new Game(word)

  private val log = LoggerFactory.getLogger(getClass)

  sendState()

  def handlePacket(packet: Packet): Unit = {
    packet match {
      case Packet.TryLetter(letter) =>
        game.tryLetter(letter)
      case Packet.TryWord(word) =>
        game.tryWord(word)
      case Packet.Restart =>
        game = new Game(word)
      case pkt =>
        log.error(s"Invalid packet: $pkt")
    }
    sendState()
    if (game.gameOver) {
      sendPacket(Packet.GameOver(game.isSolved))
    }
  }

  private def sendState(): Unit = {
    sendPacket(gameStatePacket)
  }

  private def gameStatePacket = Packet.GameState(game.triesRemaining, game.triedLetters, game.clue)
}
