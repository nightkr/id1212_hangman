package se.nullable.kth.id1212.hangman.server.model

import java.util.Scanner
import javax.inject.Provider
import scala.util.Random


class WordPicker extends Provider[Word] {
  private val words = {
    val is = getClass.getResourceAsStream("words.txt")
    try {
      val scanner = new Scanner(is)
      var list = Seq[String]()
      try {
        while (true) {
          list +:= scanner.nextLine()
        }
      } catch {
        case _: NoSuchElementException =>
      }
      list
    } finally {
      is.close()
    }
  }

  private val random = new Random

  def get(): Word = Word(words(random.nextInt(words.length)))
}
