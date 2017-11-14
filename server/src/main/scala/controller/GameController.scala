package se.nullable.kth.id1212.hangman.server.model.controller

import javax.inject.{ Inject, Provider }
import org.slf4j.LoggerFactory
import se.nullable.kth.id1212.hangman.proto.Packet
import se.nullable.kth.id1212.hangman.server.model.Game

class GameController @Inject() (gameProvider: Provider[Game]) {
  private var game = gameProvider.get

  private val log = LoggerFactory.getLogger(getClass)

  private var sendPacket: Option[Packet => Unit] = None

  def start(sendPacket: Packet => Unit): Unit = {
    this.sendPacket = Some(sendPacket)

    sendState()
  }

  def handlePacket(packet: Packet): Unit = {
    packet match {
      case Packet.TryLetter(letter) =>
        game.tryLetter(letter)
      case Packet.TryWord(word) =>
        game.tryWord(word)
      case Packet.Restart =>
        game = gameProvider.get
      case pkt =>
        log.error(s"Invalid packet: $pkt")
    }
    sendState()
    if (game.gameOver) {
      sendPacket.get(Packet.GameOver(game.isSolved))
    }
  }

  private def sendState(): Unit = {
    sendPacket.get(gameStatePacket)
  }

  private def gameStatePacket = Packet.GameState(game.triesRemaining, game.triedLetters, game.clue)
}
