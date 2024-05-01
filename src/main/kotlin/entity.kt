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
    var name: String
    var url: String
    var method: HTTPMETHOD
    var collection: Collections
    var preRequestScript: PreRequestScripts
    var testScript: TestScripts
}

interface Params: Entity<Params> {
    companion object : Entity.Factory<Params>()
    val id: Int
    var key: String
    var value: String
    var description: String
    var request: Requests
}

interface Headers: Entity<Headers> {
    companion object : Entity.Factory<Headers>()
    val id: Int
    val key: String
    val value: Clob
    val description: String
    val request: Requests
}

interface Bodies: Entity<Bodies> {
    companion object : Entity.Factory<Bodies>()
    val id: Int
    var key: String
    var value: Clob
    var description: String
    var type: BodyType
    var request: Requests
}

interface PreRequestScripts: Entity<PreRequestScripts> {
    companion object : Entity.Factory<PreRequestScripts>()
    val id: Int
    var script: Clob
}

interface TestScripts: Entity<TestScripts> {
    companion object : Entity.Factory<TestScripts>()
    val id: Int
    var script: Clob
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