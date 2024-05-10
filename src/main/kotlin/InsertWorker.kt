package ddcMan

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.last
import javax.print.Doc
import javax.swing.JTextField
import javax.swing.SwingWorker

class InsertWorker(private val tab: String, private val saveName: String, private val collection: Int): SwingWorker<Int, Unit>() {
    override fun doInBackground(): Int {
        val preScriptText = Document.get<RSyntaxTextArea>(tab, "pre_script_text_area")?.text ?: ""
        val testScriptText = ""
        Store.db.useConnection {
            val preScript = PreRequestScripts {
                script = it.createClob().apply { setString(1, preScriptText) }
            }
            val testScript = TestScripts {
                script = it.createClob().apply { setString(1, testScriptText) }
            }
            Store.preRequestScripts.add(preScript)
            Store.testScripts.add(testScript)
        }
        val preScript = Store.preRequestScripts.last()
        val newTestScript = Store.testScripts.last()
        val urlInput = Document.get<JTextField>(tab, "uri_input")!!.text
        val methodInput = Document.get<HTTPMETHOD>(tab, "method")!!
        val collectionInput = Store.collections.find { it.id eq collection }!!
        val newRequest = Requests {
            name = saveName
            url = urlInput
            method = methodInput
            collection = collectionInput
            preRequestScript = preScript
            testScript = newTestScript
        }
        Store.requests.add(newRequest)
        val params = Document.get<List<List<String>>>(tab, "param_uri_assoc")!!
        for (p in params) {
            if (p.isNotEmpty()) {
                val param = Params {
                    key = p[0]
                    value = p[1]
                    description = p[2]
                    request = newRequest
                }
                Store.params.add(param)
            }
        }
        val bodies = Document.get<List<List<String>>>(tab, "req_body")!!
        val bodyType = Document.get<BodyType>(tab, "body_type")!!
        Store.db.useConnection {
            for (b in bodies) {
                if (b.isNotEmpty()) {
                    val body = Bodies {
                        key = b[0]
                        value = it.createClob().apply { setString(1, b[1]) }
                        description = b[2]
                        type = bodyType
                        request = newRequest
                    }

                    Store.bodies.add(body)
                }
            }
        }
    return newRequest.id
    }

    override fun done() {
        try {
            val requestId = get()
            Store.rxSubject.onNext(Pair("InsertRequest|${requestId}", "$collection"))
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.cause)
        }
    }
}