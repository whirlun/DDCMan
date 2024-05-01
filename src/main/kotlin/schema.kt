package ddcMan

import ddcMan.BodyTable.bindTo
import org.ktorm.schema.*
import java.sql.Clob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

object CollectionTable: Table<Collections>("Collection") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val description = clob("description").bindTo { it.description }
}

object RequestTable: Table<Requests>("Request") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val url = varchar("url").bindTo { it.url }
    val method = enum<HTTPMETHOD>("method").bindTo { it.method }
    val collection_id = int("collection_id").references(CollectionTable) { it.collection }
    val pre_script_id = int("pre_script_id").references(PreRequestScriptTable) { it.preRequestScript }
    val test_script_id = int("test_script_id").references(TestScriptTable) { it.testScript }
}

object ParamTable: Table<Params>("Param") {
    val id = int("id").primaryKey().bindTo { it.id }
    val key = varchar("key").bindTo { it.key }
    val value = varchar("value").bindTo { it.value }
    val description = varchar("description").bindTo { it.description }
    val request_id = int("request_id").references(RequestTable) { it.request }
}

object HeaderTable: Table<Headers>("Header") {
    val id = int("id").primaryKey().bindTo { it.id }
    val key = varchar("key").bindTo { it.key }
    val value = clob("value").bindTo { it.value }
    val description = varchar("description").bindTo { it.description }
    val request_id = int("request_id").references(RequestTable) { it.request }
}

object BodyTable: Table<Bodies>("Body") {
    val id = int("id").primaryKey().bindTo { it.id }
    val key = varchar("key").bindTo { it.key }
    val value = clob("value").bindTo { it.value }
    val description = varchar("description").bindTo { it.description }
    val type = enum<BodyType>("type").bindTo { it.type }
    val request_id = int("request_id").references(RequestTable) { it.request }
}

object PreRequestScriptTable: Table<PreRequestScripts>("PreRequestScript") {
    val id = int("id").primaryKey().bindTo { it.id }
    val script = clob("script").bindTo { it.script }
}

object TestScriptTable: Table<TestScripts>("TestScript") {
    val id = int("id").primaryKey().bindTo { it.id }
    val script = clob("script").bindTo { it.script }
}

object EnvironmentTable: Table<Environments>("Environment") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
}

object VariableTable: Table<Variables>("Variable") {
    val id = int("id").primaryKey().bindTo { it.id }
    val key = varchar("key").bindTo { it.key }
    val value = clob("value").bindTo { it.value }
    val environment_id = int("environment_id").references(EnvironmentTable) { it.environment }
}

fun BaseTable<*>.clob(name: String): Column<Clob> {
    return registerColumn(name, ClobSqlType)
}

object ClobSqlType : SqlType<Clob>(Types.CLOB, typeName = "clob") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: Clob) {
        ps.setClob(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): Clob? {
        return rs.getClob(index)
    }
}