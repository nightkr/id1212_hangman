package se.nullable.kth.id1212.hangman.client.view

import javafx.application.Platform
import javafx.beans.binding.{Bindings, StringExpression}
import javafx.beans.property.{BooleanProperty, IntegerProperty, SimpleBooleanProperty, SimpleIntegerProperty, SimpleStringProperty, StringProperty}
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.{ Button, Label, TextInputDialog }
import javafx.scene.input.KeyEvent
import javafx.scene.layout.{ Pane, StackPane }
import javafx.scene.shape.SVGPath
import javax.swing.JDialog
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import se.nullable.kth.id1212.hangman.client.controller.{ClientController, UpdateListener}
import se.nullable.kth.id1212.hangman.proto.Packet

class ViewController(controller: ClientController) {
  @FXML
  var window: Pane = _

  @FXML
  var hangmanPath: SVGPath = _

  @FXML
  var clueLbl: Label = _

  @FXML
  var triesRemainingLbl: Label = _

  @FXML
  var statesPane: Pane = _

  @FXML
  var inProgressPane: Node = _

  @FXML
  var lostPane: Node = _

  @FXML
  var wonPane: Node = _

  @FXML
  var restartBtn: Button = _

  @FXML
  var guessWordBtn: Button = _

  val triesRemaining: IntegerProperty = new SimpleIntegerProperty(0)
  val clue: StringProperty = new SimpleStringProperty("")

  val gameOver: BooleanProperty = new SimpleBooleanProperty(false)
  val won: BooleanProperty = new SimpleBooleanProperty(false)

  val hangmanPathValue: StringExpression = Bindings.createStringBinding(() => HangmanLoader.path(triesRemaining.get), triesRemaining)

  private def updateStatePane(): Unit = {
    statesPane.getChildren.setAll(
      if (gameOver.get) {
        if (won.get) {
          wonPane
        } else {
          lostPane
        }
      } else {
        inProgressPane
      }
    )
  }

  @FXML
  def initialize(): Unit = {
    hangmanPath.contentProperty().bind(hangmanPathValue)
    clueLbl.textProperty().bind(clue)
    triesRemainingLbl.textProperty().bind(triesRemaining.asString())

    gameOver.addListener(_ => updateStatePane())
    won.addListener(_ => updateStatePane())
    updateStatePane()

    restartBtn.setOnAction(ev => controller.restart())
    guessWordBtn.setOnAction { ev =>
      val dialog = new TextInputDialog()
      dialog.setHeaderText("Enter your guess:")
      dialog.setTitle("NetHangman - Guess Word")
      val value = dialog.showAndWait()
      if (value.isPresent()) {
        controller.tryWord(value.get)
      }
    }

    window.addEventHandler(KeyEvent.KEY_PRESSED,
                           { ev: KeyEvent =>
                             val code = ev.getCode
                             if (code.isLetterKey()) {
                               controller.tryLetter(code.name.toLowerCase()(0))
                             }
                           }
    )
  }

  object UpdateListener extends UpdateListener {
    def gameOver(win: Boolean): Unit = {
      Platform.runLater { () =>
        won.set(win)
        ViewController.this.gameOver.set(true)
      }
    }

    def gameStateUpdate(state: Packet.GameState): Unit = {
      Platform.runLater { () =>
        triesRemaining.set(state.triesRemaining)
        clue.set(state.clue.map(_.getOrElse('_')).mkString)
        ViewController.this.gameOver.set(false)
      }
    }
  }
}

object HangmanLoader {
  val paths: Seq[String] = {
    val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val xpath = XPathFactory.newInstance().newXPath()
    val stream = getClass.getResourceAsStream("hangman.svg")
    val document = try {
      parser.parse(stream)
    } finally {
      stream.close()
    }
    (Stream
      .from(0)
      .map(index => xpath.evaluate(s"//path[@id='lives$index']//@d", document))
       .takeWhile(_.nonEmpty).toList
       :+ xpath.evaluate(s"//path[@id='frame']//@d", document))
      .map("M 0,0 " + _) // Start each subpath at 0,0
  }

  def path(triesRemaining: Int): String = HangmanLoader.paths.drop(triesRemaining).mkString(" ")
}
