package ddcMan

import io.reactivex.rxjava3.core.Observable
import javax.swing.JLabel
import javax.swing.JTextField
import io.reactivex.rxjava3.kotlin.toObservable
import java.util.concurrent.TimeUnit

object Document {
    val store: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()
    fun put(tab: String, key: String, value: Any) {
        store[tab]?.set(key, value)
    }

    inline fun <reified T> get(tab: String, key: String): T? = store[tab]?.get(key) as? T

    fun hasTab(tab: String): Boolean = store.containsKey(tab)

    fun addDocument(tab: String) {
        store[tab] = mutableMapOf(
            "id" to -1,
            "uri_input" to JTextField("http://127.0.0.1:4567/post-test"),
            "param_uri_assoc" to mapOf("" to ""),
            "req_body" to mapOf(":;Text" to ""),
            "result_text_area" to themedSyntaxTextArea(),
            "pre_script_text_area" to themedSyntaxTextArea(),
            "raw_text_area" to themedSyntaxTextArea(),
            "status" to JLabel(""),
            "body_type" to BodyType.NONE
        )
    }
}