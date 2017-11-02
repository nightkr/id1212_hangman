package se.nullable.kth.id1212.hangman.proto

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, EOFException}

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.prop.PropertyChecks

class PacketReaderSpec extends WordSpec with Matchers with PropertyChecks {
  private def seqReader(bytes: Byte*): PacketReader = new PacketReader(new ByteArrayInputStream(bytes.toArray))

  "A PacketReader given an InputStream" when {
    "empty" should {
      "throw an EOFException" in {
        val reader = seqReader()
        assertThrows[EOFException](reader.readNext())
      }
    }

    "containing an invalid packet type" should {
      "throw an InvalidPacketException" in {
        val reader = seqReader(
          0, 0, 0, 4, // Packet length
          0, 0, 0, 0  // Type 0 (reserved invalid)
        )
        assertThrows[InvalidPacketException](reader.readNext())
      }
    }

    "reading a TryLetter packet" should {
      "decode the packet" in {
        val reader = seqReader(
          0, 0, 0, 5, // Packet length
          0, 0, 0, 1, // Type 1 (TryLetter)
          'c'.toByte
        )
        reader.readNext() shouldEqual Packet.TryLetter('c')
      }
    }

    "reading a GameState packet" should {
      "decode the packet" in {
        val reader = seqReader(
          0, 0, 0, 24, // Packet length
          0, 0, 0, 2,  // Type 2 (GameState)
          0, 0, 0, 5,  // Remaining tries
          0, 0, 0, 2,  // Tried character count
          'g'.toByte, 'h'.toByte,
          0, 0, 0, 6,  // Clue length
          'q'.toByte, 'w'.toByte, 'e'.toByte, 'r'.toByte, 't'.toByte, 'y'.toByte
        )
        reader.readNext() shouldEqual Packet.GameState(5, Set('g', 'h'), "qwerty")
      }
    }
  }

  "A PacketReader" when {
    "given a value serialized by PacketWriter" should {
      val genPacketTryLetter: Gen[Packet.TryLetter] = for {
        letter <- Gen.alphaChar
      } yield Packet.TryLetter(letter)
      val genPacketGameState: Gen[Packet.GameState] = for {
        triesLeft <- Gen.choose(0, 500)
        triedChars <- Gen.listOf(Gen.alphaLowerChar)
        clue <- Gen.alphaNumStr
      } yield Packet.GameState(triesLeft, triedChars.toSet, clue)

      implicit val genPacket: Arbitrary[Packet] = Arbitrary(Gen.oneOf(genPacketTryLetter, genPacketGameState))

      "give back the same value" in {
        forAll { packet: Packet =>
          val bos = new ByteArrayOutputStream
          val writer = new PacketWriter(bos)
          writer.write(packet)
          val reader = new PacketReader(new ByteArrayInputStream(bos.toByteArray()))
          val readPkt = reader.readNext()
          readPkt shouldEqual packet
        }
      }
    }
  }
}
