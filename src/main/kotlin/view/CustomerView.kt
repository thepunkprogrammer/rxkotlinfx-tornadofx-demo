package view

import app.Styles
import app.toSet
import domain.Customer
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.SelectionMode
import javafx.scene.paint.Color
import rx.javafx.kt.actionEvents
import rx.javafx.kt.addTo
import rx.javafx.kt.onChangedObservable
import rx.javafx.kt.plusAssign
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.subscribeWith
import rx.lang.kotlin.toObservable
import tornadofx.*

class CustomerView : View() {
    private val controller: EventController by inject()

    override val root = borderpane {
        top = label("CUSTOMER").addClass(Styles.heading)

        center = tableview<Customer> {
            column("ID", Customer::id)
            column("NAME", Customer::name)

            selectionModel.selectionMode = SelectionMode.MULTIPLE

            //broadcast selections
            controller.selectedCustomers += selectionModel.selectedItems.onChangedObservable()
                    .flatMap { it.toObservable().filterNotNull().toSet() }

            //Import data and refresh event handling
            controller.refreshCustomers.toObservable().startWith(Unit)
                    .flatMap {
                        Customer.all.toList()
                    }.subscribeWith {
                        onNext { items.setAll(it) }
                        onError { alert(Alert.AlertType.ERROR, "PROBLEM!", it.message ?: "").show() }
                    }

            //handle search request
            controller.searchCustomers.toObservable()
                .subscribeWith {
                    onNext { ids -> moveToTopWhere { it.id in ids } }
                    onError { it.printStackTrace() }
                }

        }
        left = toolbar {
            orientation = Orientation.VERTICAL
            button("⇇\uD83D\uDD0E") {
                actionEvents().flatMap {
                    controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }.addTo(controller.searchCustomerUsages)
            }
            button("⇉\uD83D\uDD0E") {
                controller.searchCustomers += actionEvents().flatMap {
                    controller.selectedSalesPeople.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .flatMap { it.customerAssignments.toObservable() }
                            .distinct()
                            .toSet()
                }.addTo(controller.searchCustomers)
            }
            button("⇇") {
                useMaxWidth = true
                textFill = Color.GREEN
                controller.applyCustomers += actionEvents().flatMap {
                    controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }
            }
            //remove selected customers
            button("⇉") {
                useMaxWidth = true
                textFill = Color.RED
                controller.removeCustomers += actionEvents().flatMap {
                    controller.selectedCustomers.toObservable().take(1)
                            .flatMap { it.toObservable() }
                            .map { it.id }
                            .toSet()
                }
            }
        }
    }
}