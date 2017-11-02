package se.nullable.kth.id1212.hangman.server.net

import java.io.Closeable
import java.net.{ InetSocketAddress, ServerSocket, Socket, SocketAddress }
import java.util.concurrent.atomic.AtomicBoolean

class Listener extends Closeable {
  private val server = new ServerSocket()
  private val stop = new AtomicBoolean(false)
  private var thread: Option[Thread] = None

  def start(): Unit = {
    close()

    server.bind(new InetSocketAddress(2729))
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
    }
  }
}
