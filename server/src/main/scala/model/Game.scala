package se.nullable.kth.id1212.hangman.server.model

import se.nullable.kth.id1212.hangman.proto.Packet


class Game(word: String) {
  private var triesRemaining = 15
  private var triedLetters = Set[Char]()

  def tryLetter(letter: Char): Unit = {
    if (triesRemaining > 0 && !triedLetters.contains(letter) && isLetter(letter)) {
      triedLetters += letter
      if (!word.contains(letter)) {
        triesRemaining -= 1
      }
    }
  }

  def isLetter(chr: Char) = chr >= 'a' && chr <= 'z'
  def clue: String = word.map {
    case chr if isLetter(chr) && !triedLetters.contains(chr) => '_'
    case chr => chr
  }

  def isSolved = clue == word
  def gameOver = triesRemaining == 0

  def toGameStatePacket: Packet.GameState = Packet.GameState(triesRemaining, triedLetters, clue)
}
