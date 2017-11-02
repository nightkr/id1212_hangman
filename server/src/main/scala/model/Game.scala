package se.nullable.kth.id1212.hangman.server.model

import se.nullable.kth.id1212.hangman.proto.Packet


class Game(word: String) {
  private var triesRemaining = 15
  private var triedLetters = Set[Char]()

  def tryLetter(letter: Char): Unit = {
    if (!triedLetters.contains(letter) && triesRemaining > 0) {
      triedLetters += letter
      triesRemaining -= 1
    }
  }

  def toGameStatePacket: Packet.GameState = Packet.GameState(triesRemaining, triedLetters, word)
}
