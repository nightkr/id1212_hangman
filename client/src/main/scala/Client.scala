package se.nullable.kth.id1212.hangman.client

import java.net.Socket
import scala.io.StdIn
import se.nullable.kth.id1212.hangman.proto.{ Packet, PacketReader, PacketWriter }


object DumbClient extends App {
  val socket = new Socket("localhost", 2729)
  val writer = new PacketWriter(socket.getOutputStream)
  val reader = new PacketReader(socket.getInputStream)
  val readThread = new Thread(
    { () =>
      while (true) {
        val pkt = reader.readNext()
        println(pkt)
      }
    }
  )
  readThread.start()
  while (true) {
    val line = StdIn.readLine()
    val char = line.charAt(0)
    writer.write(Packet.TryLetter(char))
  }
}
