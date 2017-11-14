package se.nullable.kth.id1212.hangman.server.model

import javax.inject.Inject


class Game @Inject() (word: Word) {
  private var _triesRemaining = word.str.length()
  private var _triedLetters = Set[Char]()

  def triesRemaining = _triesRemaining
  def triedLetters = _triedLetters

  def tryLetter(letter: Char): Unit = {
    if (!gameOver && !triedLetters.contains(letter) && isLetter(letter)) {
      _triedLetters += letter
      if (!word.str.contains(letter)) {
        _triesRemaining -= 1
      }
    }
  }

  def tryWord(word: String): Unit = {
    if (!gameOver) {
      if (word == this.word) {
        _triedLetters = this.word.str.toSet
      } else {
        _triesRemaining -= 1
      }
    }
  }

  def isLetter(chr: Char) = chr >= 'a' && chr <= 'z'
  def clue: Seq[Option[Char]] = word.str.map(Some(_).filter(char => !isLetter(char) || triedLetters.contains(char)))

  def isSolved = clue == word.str.map(Some(_))
  def gameOver = triesRemaining == 0 || isSolved
}

case class Word(str: String)
