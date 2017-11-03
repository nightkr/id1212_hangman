package se.nullable.kth.id1212.hangman.client

import java.io.{ BufferedReader, Reader }
import java.net.Socket
import java.util.Scanner
import java.util.concurrent.atomic.AtomicBoolean
import scala.io.StdIn
import se.nullable.kth.id1212.hangman.proto.{ Packet, PacketReader, PacketWriter }


object DumbClient extends App {
  val socket = new Socket("localhost", 2729)
  val writer = new PacketWriter(socket.getOutputStream)
  val reader = new PacketReader(socket.getInputStream)
  val readThread = new Thread(
    { () =>
      try {
        while (true) {
          val pkt = reader.readNext()
          println(pkt)
        }
      } finally {
        System.exit(0)
      }
    }
  )
  readThread.setDaemon(true)
  readThread.start()
  try {
    val in = new Scanner(System.in)
    while (!Thread.currentThread().isInterrupted()) {
      in.nextLine() match {
        case "" =>
          System.exit(0)
        case line =>
          val char = line.charAt(0)
          writer.write(Packet.TryLetter(char))
      }
    }
  } finally {
    socket.close()
  }
}
