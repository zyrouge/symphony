package io.github.zyrouge.symphony.utils.lazy_linked_list

typealias LazyLinkedListInsertAppendOperationCreateFn<K, V, X> = (value: X, isHead: Boolean, nextId: K?) -> V

class LazyLinkedListInsertAppendOperation<K, V, X>(
    val operator: LazyLinkedListOperator<K, V>,
    val insertAfterId: K?,
    val values: List<X>,
    val createFn: LazyLinkedListInsertAppendOperationCreateFn<K, V, X>,
) : LazyLinkedListOperator.Operation<K, V> {
    override suspend fun perform(): LazyLinkedListOperator.Changeset<K, V> {
        if (values.isEmpty()) {
            return LazyLinkedListOperator.Changeset()
        }
        val addedKeys = mutableListOf<K>()
        val added = mutableListOf<V>()
        val modifiedKeys = mutableListOf<K>()
        val modified = mutableListOf<V>()
        val insertAfterEntity = insertAfterId?.let { operator.persistenceFunctions.getEntity(it) }
            ?: operator.persistenceFunctions.getTailEntity()
        val hasHead = insertAfterEntity != null
        var nextId = insertAfterEntity?.let { operator.entityFunctions.getEntityNextId(it) }
        for (i in (values.size - 1) downTo 0) {
            val value = values[i]
            val isHead = i == 0 && !hasHead
            val entity = createFn(value, isHead, nextId)
            val id = operator.entityFunctions.getEntityId(entity)
            addedKeys.add(id)
            added.add(entity)
            nextId = id
        }
        if (insertAfterEntity != null) {
            val insertAfterId = operator.entityFunctions.getEntityId(insertAfterEntity)
            val nInsertAfterEntity =
                operator.entityFunctions.updateEntityNextId(insertAfterEntity, nextId)
            modifiedKeys.add(insertAfterId)
            modified.add(nInsertAfterEntity)
        }
        return LazyLinkedListOperator.Changeset(
            addedKeys = addedKeys,
            addedEntities = added,
            modifiedKeys = modifiedKeys,
            modifiedEntities = modified,
        )
    }
}
