package se.nullable.kth.id1212.hangman.server.net

import java.io.{ Closeable, EOFException }
import java.net.{ InetSocketAddress, ServerSocket, Socket, SocketAddress, SocketException }
import java.util.concurrent.atomic.AtomicBoolean
import se.nullable.kth.id1212.hangman.proto.{ PacketReader, PacketWriter }
import se.nullable.kth.id1212.hangman.server.model.controller.GameController

class Listener extends Closeable {
  private val stop = new AtomicBoolean(false)
  private var thread: Option[Thread] = None

  def start(): Unit = {
    close()

    val server = new ServerSocket(2729)
    val t = new ListenerThread(server, stop)
    t.start()
    thread = Some(t)
  }

  override def close(): Unit = {
    stop.set(true)
    thread.foreach(_.join())
    stop.set(false)
  }
}

class ListenerThread(server: ServerSocket, stopRequest: AtomicBoolean) extends Thread {
  override def run(): Unit = {
    while (!stopRequest.get) {
      val socket = server.accept()
      val reader = new PacketReader(socket.getInputStream)
      val writer = new PacketWriter(socket.getOutputStream)
      val ctrl = new GameController(writer.write, () => socket.close())
      try {
        while (true) {
          val packet = reader.readNext()
          ctrl.handlePacket(packet)
        }
      } catch {
        case _: EOFException =>
          socket.close()
        case _: SocketException =>
      }
    }
    server.close()
  }
}
