package se.nullable.kth.id1212.hangman.server

import com.google.inject.{ Binder, Module }
import se.nullable.kth.id1212.hangman.server.model.{ Word, WordPicker }


class ServerModule extends Module {
  def configure(binder: Binder): Unit = {
    binder.bind(classOf[Word]).toProvider(classOf[WordPicker])
  }
}
