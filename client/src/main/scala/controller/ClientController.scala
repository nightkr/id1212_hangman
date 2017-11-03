package se.nullable.kth.id1212.hangman.client.controller

import scala.collection.mutable.MutableList
import se.nullable.kth.id1212.hangman.proto.Packet
import se.nullable.kth.id1212.hangman.client.net.Connection

class ClientController {
  private val listeners = MutableList[UpdateListener]()
  private val connection = new Connection(receivePacket)

  def addListener(listener: UpdateListener): Unit = {
    listeners += listener
  }

  def start(): Unit = {
    connection.start()
  }

  def stop(): Unit = {
    connection.stop()
  }

  private def receivePacket(packet: Packet): Unit = packet match {
    case pkt: Packet.GameState =>
      listeners.foreach(_.gameStateUpdate(pkt))
    case pkt: Packet.GameOver =>
      listeners.foreach(_.gameOver(pkt.win))
  }

  def tryLetter(letter: Char): Unit = {
    connection.sendPacket(Packet.TryLetter(letter))
  }
}

trait UpdateListener {
  def gameStateUpdate(state: Packet.GameState): Unit
  def gameOver(win: Boolean): Unit
}
