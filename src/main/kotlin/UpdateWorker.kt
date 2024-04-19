package ddcMan

import org.ktorm.dsl.eq
import org.ktorm.entity.find
import javax.swing.SwingWorker

class UpdateWorker(private val collection: CollectionNode, private val name: String): SwingWorker<Unit, Unit>() {
    override fun doInBackground() {
        val col = Store.collections.find { row -> row.id eq collection.id }
        if (col != null) {
            col.name = name
            if (col.flushChanges() != 0) {
                Store.rxSubject.onNext(Pair("CollectionName|${collection.id}", name))
            }
        }
    }

    override fun done() {
        super.done()
    }
}