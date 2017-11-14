package se.nullable.kth.id1212.hangman.server

import com.google.inject.Guice
import javax.inject.Inject
import scala.io.StdIn

import org.slf4j.LoggerFactory
import se.nullable.kth.id1212.hangman.server.net.Listener

class Server @Inject() (listener: Listener) {
  private val log = LoggerFactory.getLogger(getClass)

  def run(): Unit = {
    listener.start()
    try {
      log.info("Listening... (press ENTER to stop)")
      StdIn.readLine()
    } finally {
      listener.close()
    }
  }
}

object Server extends App {
  val injector = Guice.createInjector(new ServerModule)
  injector.getInstance(classOf[Server]).run()
}
