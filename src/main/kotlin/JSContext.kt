package ddcMan

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value

object JSContext {
    private var enabled: Boolean = true
    private val ctx: Context?
    init {
        val path = getPlatformDataDir()
        val options = mapOf(
            "js.commonjs-require" to "true",
            "js.commonjs-require-cwd" to path
        )
        ctx = Context.newBuilder("js")
            .allowExperimentalOptions(true)
            .allowIO(true)
            .options(options)
            .build()
    }

    fun isEnabled(): Boolean = enabled

    fun evalJs(code: String): Value? {
        if (!isEnabled()) return null
        return ctx!!.eval("js", code)
    }

    fun getJsFunction(file: String, func: String): Value? {
        if (!isEnabled()) return null
        val jsModule = ctx!!.eval("js", "require('./js/$file')")

        return jsModule.getMember(func)
    }
}
