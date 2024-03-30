package ddcMan

import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate.Param
import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import java.io.BufferedReader
import java.io.InputStreamReader

object Store {
    val db = Database.connect("jdbc:h2:file:${getPlatformDataDir()}/ddcman;CASE_INSENSITIVE_IDENTIFIERS=TRUE")

    init {
        val schemaScript = javaClass.getResourceAsStream("/schema.sql")
            ?.bufferedReader()
            .use { it?.readText() ?: "" }
        db.useConnection { conn ->
            conn.prepareStatement(schemaScript).use { stmt ->
                stmt.execute()
            }
        }
    }

    val collections get() = db.sequenceOf(CollectionTable)
    val requests get() = db.sequenceOf(RequestTable)
    val params get() = db.sequenceOf(ParamTable)
    val headers get() = db.sequenceOf(HeaderTable)
    val bodies get() = db.sequenceOf(BodyTable)
    val preRequestScripts get() = db.sequenceOf(PreRequestScriptTable)
    val testScripts get() = db.sequenceOf(TestScriptTable)
    val environments get() = db.sequenceOf(EnvironmentTable)
    val variables get() = db.sequenceOf(VariableTable)
}