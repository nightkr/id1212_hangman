package se.nullable.kth.id1212.hangman.server.net

import java.io.Closeable
import java.net.InetSocketAddress
import java.nio.channels.{ClosedChannelException, SelectionKey, Selector, ServerSocketChannel, SocketChannel}

import scala.collection.JavaConverters._

import javax.inject.{Inject, Provider}
import org.slf4j.LoggerFactory
import se.nullable.kth.id1212.hangman.proto.{AsyncPacketReader, AsyncPacketWriter}
import se.nullable.kth.id1212.hangman.server.model.controller.GameController

class Listener @Inject() (controllerProvider: Provider[GameController]) extends Closeable {
  private var thread: Option[(Thread, ServerSocketChannel)] = None

  def start(port: Int): Unit = {
    close()

    val server = ServerSocketChannel.open()
    server.bind(new InetSocketAddress(port))
    server.configureBlocking(false)
    val t = new ListenerThread(server, controllerProvider)
    t.start()
    thread = Some((t, server))
  }

  override def close(): Unit = {
    thread.foreach { case (t, socket) =>
      socket.close()
      t.interrupt()
      t.join()
    }
    thread = None
  }
}

class ListenerThread(server: ServerSocketChannel, controllerProvider: Provider[GameController]) extends Thread {
  setDaemon(true)

  private val log = LoggerFactory.getLogger(getClass)
  private val selector = Selector.open()
  server.register(selector, SelectionKey.OP_ACCEPT)

  override def run(): Unit = {
    log.info(s"Listening on ${server.getLocalAddress}")
    try {
      while (server.isOpen()) {
        selector.select()
        for (key <- selector.selectedKeys().asScala) {
          selector.selectedKeys().remove(key)
          key.channel() match {
            case serverChan: ServerSocketChannel =>
              if (key.isAcceptable()) {
                Option(serverChan.accept()).foreach { socket =>
                  socket.configureBlocking(false)
                  val key = socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE)
                  log.debug(s"New connection from $socket")
                  val conn = new ConnectionHandler(socket, controllerProvider)
                  conn.start()
                  key.attach(conn)
                }
              }
            case socket: SocketChannel =>
              if (key.isReadable()) {
                key.attachment.asInstanceOf[ConnectionHandler].read()
              }
              if (key.isValid() && key.isWritable()) {
                key.attachment.asInstanceOf[ConnectionHandler].write()
              }
          }
        }
      }
    } finally {
      for (key <- selector.keys().asScala) {
        if (key.channel().isOpen()) {
          key.channel().close()
        }
      }
      selector.close()
    }
  }
}

class ConnectionHandler(socket: SocketChannel, controllerProvider: Provider[GameController]) {
  private val reader = new AsyncPacketReader(socket)
  private val writer = new AsyncPacketWriter(socket)
  private val ctrl = controllerProvider.get
  private val log = LoggerFactory.getLogger(getClass)

  def start(): Unit = {
    ctrl.start(writer.write)
  }

  def stop(): Unit = {
    log.debug(s"Disconnected from $socket")
    socket.close()
  }

  def read(): Unit = {
    try {
      Stream.continually(reader.readNext())
        .takeWhile(_.isDefined)
        .flatten
        .foreach(ctrl.handlePacket)
    } catch {
      case _: ClosedChannelException =>
        stop()
    }
  }

  def write(): Unit = {
    try {
      writer.flush()
    } catch {
      case _: ClosedChannelException =>
        stop()
    }
  }
}
