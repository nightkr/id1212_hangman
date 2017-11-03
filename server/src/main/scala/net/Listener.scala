package se.nullable.kth.id1212.hangman.server.net

import java.io.{Closeable, EOFException}
import java.net.{ServerSocket, Socket, SocketException}
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory

import se.nullable.kth.id1212.hangman.proto.{PacketReader, PacketWriter}
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
    thread.foreach { t =>
      new Socket("localhost", 2729).close() // Interrupt the accept loop
      t.join()
    }
    thread = None
    stop.set(false)
  }
}

class ListenerThread(server: ServerSocket, stopRequest: AtomicBoolean) extends Thread {
  setDaemon(true)

  private val log = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    try {
      while (!stopRequest.get) {
        val socket = server.accept()
        log.debug(s"New connection from $socket")
        val conn = new ConnectionThread(socket)
        conn.start()
      }
    } finally {
      server.close()
    }
  }
}

class ConnectionThread(socket: Socket) extends Thread {
  setDaemon(true)

  private val log = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    try {
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
        case _: SocketException =>
      }
    } finally {
      log.debug(s"Disconnected from $socket")
      socket.close()
    }
  }
}
