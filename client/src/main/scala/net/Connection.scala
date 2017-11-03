package se.nullable.kth.id1212.hangman.client.net

import java.io.EOFException
import java.net.{ InetSocketAddress, Socket }
import se.nullable.kth.id1212.hangman.proto.{ Packet, PacketReader, PacketWriter }

class Connection(packetListener: Packet => Unit) {
  private val socket: Socket = new Socket()
  private var writer: Option[PacketWriter] = None

  def start(): Unit = {
    socket.connect(new InetSocketAddress("127.0.0.1", 2729))
    writer = Some(new PacketWriter(socket.getOutputStream))
    val reader = new PacketReader(socket.getInputStream)
    new ReaderThread(reader, packetListener).start()
  }

  def stop(): Unit = {
    socket.close()
  }

  def sendPacket(packet: Packet): Unit = {
    writer.get.write(packet)
  }
}

class ReaderThread(reader: PacketReader, listener: Packet => Unit) extends Thread {
  setDaemon(true)

  override def run(): Unit = {
    try {
      while(true) {
        listener(reader.readNext())
      }
    } catch {
      case _: EOFException =>
        // Game over, terminate loop
    }
  }
}
