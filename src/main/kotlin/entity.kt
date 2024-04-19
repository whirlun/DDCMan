package ddcMan

import org.ktorm.entity.Entity
import java.sql.Clob

data class CollectionNode(
    val id: Int,
    var name: String,
    val description: String) {
    constructor(collection: Collections) :
            this(collection.id,
                collection.name,
                collection.description.run { characterStream.readText() })
    override fun toString(): String = name
}

interface Collections: Entity<Collections> {
    companion object : Entity.Factory<Collections>()
    val id: Int
    var name: String
    var description: Clob
}

interface Requests: Entity<Requests> {
    companion object : Entity.Factory<Requests>()
    val id: Int
    val name: String
    val url: String
    val method: HTTPMETHOD
    val collection: Collections
    val param: Params
    val header: Headers
    val body: Bodies
    val preRequestScript: PreRequestScripts
    val testScript: TestScripts
}

interface Params: Entity<Params> {
    companion object : Entity.Factory<Params>()
    val id: Int
    val key: String
    val value: String
    val description: String
}

interface Headers: Entity<Headers> {
    companion object : Entity.Factory<Headers>()
    val id: Int
    val key: String
    val value: Clob
    val description: String
}

interface Bodies: Entity<Bodies> {
    companion object : Entity.Factory<Bodies>()
    val id: Int
    val key: String
    val value: Clob
    val description: String
    val type: BodyType
}

interface PreRequestScripts: Entity<PreRequestScripts> {
    companion object : Entity.Factory<PreRequestScripts>()
    val id: Int
    val script: Clob
}

interface TestScripts: Entity<TestScripts> {
    companion object : Entity.Factory<TestScripts>()
    val id: Int
    val script: Clob
}

interface Environments: Entity<Environments> {
    companion object : Entity.Factory<Environments>()
    val id: Int
    val name: String
}

interface Variables: Entity<Variables> {
    val id: Int
    val key: String
    val value: Clob
    val environment: Environments
}