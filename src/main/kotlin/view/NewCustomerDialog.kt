package view

import domain.Customer
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.image.ImageView
import javafx.stage.Stage
import rx.Observable
import tornadofx.*

class NewCustomerDialog: Dialog<Observable<Int>>() {
    private val root = Form()

    init {
        title = "Create New Customer"

        with(root) {
            fieldset("Customer Name") {
                textfield {
                    setResultConverter {
                        if (it == ButtonType.OK)
                            Customer.createNew(text) //returns ID for new Customer
                        else
                            Observable.empty()
                    }
                }
            }
        }

        dialogPane.content = root
        dialogPane.buttonTypes.addAll(ButtonType.OK,ButtonType.CANCEL)
        graphic = ImageView(FX.primaryStage.icons[0])
        (dialogPane.scene.window as Stage).icons += FX.primaryStage.icons[0]
    }
}