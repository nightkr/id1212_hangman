package se.nullable.kth.id1212.hangman.client.view

import javafx.fxml.FXML
import javafx.scene.control.{ DialogPane, TextField }


class ConnectViewController {
  @FXML
  var hostField: TextField = _
  @FXML
  var portField: TextField = _

  lazy val host = hostField.textProperty()
  lazy val port = portField.textProperty()
}
