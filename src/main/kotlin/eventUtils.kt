package ddcMan

import javax.swing.JTextField
import javax.swing.event.TableModelEvent
import javax.swing.table.DefaultTableModel

fun registerTableEvent(model: DefaultTableModel,
                       tab: String, doc: String,
                       defaultRow: Array<String>,
                       f: (Int) -> List<String?>,
                       fin: (() -> Unit)?) {
    model.addTableModelListener {
        if (it.type == TableModelEvent.UPDATE) {
            if (it.lastRow == model.rowCount - 1) {
                model.addRow(defaultRow)
            }
        }
        Document.put(tab, doc, IntRange(0, model.rowCount - 1).map { i -> f(i) })
        if (fin != null) {
            fin()
        }
    }
}

fun generateUriParams(tab: String): String {
    val paramUriAssoc = Document.get<List<List<String?>>>(tab, "param_uri_assoc")
    return paramUriAssoc!!.map {
        buildString {
            append(it[0])
            append(if (it[1] == null || it[1]?.isBlank() == true) "" else "=" + it[1])
            append(if (it.all {s -> s?.isBlank() == true }) "" else "&")
        }
    }.joinToString("")
}

fun syncUriParams(tab: String, origin: String) {
    if (origin == "params_table") {
        val uriInput = Document.get<JTextField>(tab, "uri_input")
        val queryStr = generateUriParams(tab).dropLast(1)
        val index = uriInput!!.text.indexOf('?')
        if (index != -1) {
            uriInput.text = uriInput.text.substring(0, index + 1) + queryStr
        } else {
            uriInput.text = uriInput.text + "?" + queryStr
        }
    }
}