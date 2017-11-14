package se.nullable.kth.id1212.hangman.server.net

import java.io.{Closeable, EOFException}
import java.net.{ServerSocket, Socket, SocketException}
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.{ Inject, Provider }
import org.slf4j.LoggerFactory

import se.nullable.kth.id1212.hangman.proto.{PacketReader, PacketWriter}
import se.nullable.kth.id1212.hangman.server.model.controller.GameController

class Listener @Inject() (controllerProvider: Provider[GameController]) extends Closeable {
  private var thread: Option[(Thread, ServerSocket)] = None

  def start(port: Int): Unit = {
    close()

    val server = new ServerSocket(port)
    val t = new ListenerThread(server, controllerProvider)
    t.start()
    thread = Some((t, server))
  }

  override def close(): Unit = {
    thread.foreach { case (t, socket) =>
      socket.close()
      t.join()
    }
    thread = None
  }
}

class ListenerThread(server: ServerSocket, controllerProvider: Provider[GameController]) extends Thread {
  setDaemon(true)

  private val log = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    try {
      while (!server.isClosed()) {
        val socket = server.accept()
        log.debug(s"New connection from $socket")
        val conn = new ConnectionThread(socket, controllerProvider)
        conn.start()
      }
    } catch {
      case ex: SocketException =>
        if (server.isClosed()) {
          // Socket is closed, terminate listening thread
        } else {
          throw ex
        }
    } finally {
      server.close()
    }
  }
}

class ConnectionThread(socket: Socket, controllerProvider: Provider[GameController]) extends Thread {
  setDaemon(true)

  private val log = LoggerFactory.getLogger(getClass)

  override def run(): Unit = {
    try {
      val reader = new PacketReader(socket.getInputStream)
      val writer = new PacketWriter(socket.getOutputStream)
      val ctrl = controllerProvider.get
      ctrl.start(writer.write)
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
