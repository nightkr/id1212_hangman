package se.nullable.kth.id1212.hangman.server

import scala.io.StdIn
import se.nullable.kth.id1212.hangman.server.net.Listener

class Server {
  def run(): Unit = {
    val listener = new Listener
    listener.start()
    println("Listening...")
    StdIn.readLine()
    listener.close()
  }
}

object Server extends App {
  new Server().run()
}
