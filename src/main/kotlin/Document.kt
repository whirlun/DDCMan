package ddcMan

import io.reactivex.rxjava3.core.Observable
import javax.swing.JLabel
import javax.swing.JTextField
import io.reactivex.rxjava3.kotlin.toObservable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

object Document {
    val store: ConcurrentMap<String, ConcurrentMap<String, Any>> = ConcurrentHashMap()
    fun put(tab: String, key: String, value: Any) {
        store[tab]?.set(key, value)
    }

    inline fun <reified T> get(tab: String, key: String): T? = store[tab]?.get(key) as? T

    fun hasTab(tab: String): Boolean = store.containsKey(tab)

    fun addTab(tab: String) {
        store[tab] = ConcurrentHashMap()
    }

    fun addDocument(tab: String) {
        store[tab] = ConcurrentHashMap(mutableMapOf(
            "id" to -1,
            "uri_input" to JTextField("http://127.0.0.1:4567/post-test"),
            "param_uri_assoc" to listOf(listOf("", "", "")),
            "req_body" to listOf(listOf("", "", "")),
            "result_text_area" to themedSyntaxTextArea(),
            "pre_script_text_area" to themedSyntaxTextArea(),
            "raw_text_area" to themedSyntaxTextArea(),
            "status" to JLabel(""),
            "body_type" to BodyType.NONE,
            "method" to HTTPMETHOD.GET
        ))
    }
}