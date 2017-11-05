package se.nullable.kth.id1212.hangman.server

import scala.io.StdIn

import org.slf4j.LoggerFactory
import se.nullable.kth.id1212.hangman.server.net.Listener

class Server {
  private val log = LoggerFactory.getLogger(getClass)

  def run(): Unit = {
    val listener = new Listener
    listener.start()
    try {
      log.info("Listening... (press ENTER to stop)")
      StdIn.readLine()
    } finally {
      listener.close()
    }
  }
}

object Server extends App {
  new Server().run()
}
