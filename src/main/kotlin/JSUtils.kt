package ddcMan

import org.graalvm.polyglot.Value

object JSUtils {
    private val _jsonBeautify: Value?

    init {
        if (JSContext.isEnabled()) {
            _jsonBeautify = JSContext.getJsFunction("beautify.js", "js_beautify")
        } else {
            _jsonBeautify = null
        }
    }

    fun jsonBeautify(json: String): String? =
        _jsonBeautify?.execute(json)?.asString()
}