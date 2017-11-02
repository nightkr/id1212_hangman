package se.nullable.kth.id1212.hangman.proto

import java.io.{ ByteArrayInputStream, EOFException, InputStream }
import java.util.Arrays

class PacketReader(stream: InputStream) {
  private def readToBuf(is: InputStream, buf: Array[Byte]): Unit = {
    var offset = 0
    while (offset < buf.length) {
      val read = is.read(buf, offset, buf.length - offset)
      if (read == -1) {
        throw new EOFException("Out of data")
      } else {
        offset += read
      }
    }
  }

  private def readByte(is: InputStream): Byte = {
    val buf = new Array[Byte](1)
    readToBuf(is, buf)
    buf(0)
  }

  private def readInt(is: InputStream): Int = {
    val buf = new Array[Byte](4)
    readToBuf(is, buf)
    var value = 0
    for (i <- 0 until 4) {
      value += ((buf(i).toInt & 0xFF) << (3 - i) * 8)
    }
    value
  }

  private def readChar(is: InputStream): Char = readByte(is).toChar

  private def readString(is: InputStream): String = {
    val length = readInt(is)
    val buf = new Array[Char](length)
    for (i <- 0 until buf.length) {
      buf(i) = readChar(is)
    }
    new String(buf)
  }

  private def readFrame(): InputStream = {
    val length = readInt(stream)
    if (length > 5000) {
      throw new InvalidPacketException(s"Frame is too long: $length > 5000")
    }
    val buf = new Array[Byte](length)
    readToBuf(stream, buf)
    new ByteArrayInputStream(buf)
  }

  def readNext(): Packet = {
    val frame = readFrame()
    val typeCode = readInt(frame)
    try {
      typeCode match {
        case Packet.Types.TRY_LETTER =>
          val character = readChar(frame)
          return Packet.TryLetter(character)
        case Packet.Types.GAME_STATE =>
          val triesLeft = readInt(frame)
          val triedChars = readString(frame)
          val clue = readString(frame)
          return Packet.GameState(triesLeft, triedChars.toSet, clue)
        case _ =>
          throw new InvalidPacketException("Unknown packet type")
      }
    } catch {
      case e: Exception =>
        throw new InvalidPacketException(s"Failed to parse packet type: $typeCode")
    }
  }
}
