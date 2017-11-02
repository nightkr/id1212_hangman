package se.nullable.kth.id1212.hangman.proto

import java.io.{ ByteArrayOutputStream, OutputStream }


class PacketWriter(stream: OutputStream) {
  private def writeInt(os: OutputStream, value: Int): Unit = {
    val buf = Array((value >> 24).toByte,
                    (value >> 16).toByte,
                    (value >> 8).toByte,
                    value.toByte)
    os.write(buf)
  }

  private def writeChar(os: OutputStream, value: Char): Unit = {
    val buf = Array(value.toByte)
    os.write(buf)
  }

  private def writeString(os: OutputStream, value: String): Unit = {
    writeInt(os, value.length())
    value.foreach(writeChar(os, _))
  }

  def write(packet: Packet): Unit = {
    val frame = new ByteArrayOutputStream()
    packet match {
      case pkt: Packet.TryLetter =>
        writeInt(frame, Packet.Types.TRY_LETTER)
        writeChar(frame, pkt.letter)
      case pkt: Packet.GameState =>
        writeInt(frame, Packet.Types.GAME_STATE)
        writeInt(frame, pkt.triesRemaining)
        writeString(frame, pkt.triedLetters.mkString)
        writeString(frame, pkt.clue)
    }
    writeInt(stream, frame.size())
    frame.writeTo(stream)
  }
}
