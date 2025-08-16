package io.github.zyrouge.symphony.utils.lazy_linked_list

typealias LazyLinkedListInsertAppendOperatorCreateFn<K, V, X> = (value: X, isHead: Boolean, nextId: K?) -> V

class LazyLinkedListInsertAppendOperator<K, V, X>(
    val helper: LazyLinkedListOperatorHelper<K, V>,
    val insertAfterId: K?,
    val values: List<X>,
    val createFn: LazyLinkedListInsertAppendOperatorCreateFn<K, V, X>,
) {
    suspend fun operate(): LazyLinkedListOperatorHelper.Result<K> {
        if (values.isEmpty()) {
            return LazyLinkedListOperatorHelper.Result()
        }
        val addedKeys = mutableListOf<K>()
        val added = mutableListOf<V>()
        val modifiedKeys = mutableListOf<K>()
        val modified = mutableListOf<V>()
        val insertAfterEntity = insertAfterId?.let { helper.persistenceFunctions.getEntity(it) }
            ?: helper.persistenceFunctions.getTailEntity()
        val hasHead = insertAfterEntity != null
        var nextId = insertAfterEntity?.let { helper.entityFunctions.getEntityNextId(it) }
        for (i in (values.size - 1) downTo 0) {
            val value = values[i]
            val isHead = i == 0 && !hasHead
            val entity = createFn(value, isHead, nextId)
            val id = helper.entityFunctions.getEntityId(entity)
            addedKeys.add(id)
            added.add(entity)
            nextId = id
        }
        if (insertAfterEntity != null) {
            val insertAfterId = helper.entityFunctions.getEntityId(insertAfterEntity)
            val nInsertAfterEntity =
                helper.entityFunctions.updateEntityNextId(insertAfterEntity, nextId)
            modifiedKeys.add(insertAfterId)
            modified.add(nInsertAfterEntity)
        }
        if (added.isNotEmpty()) {
            helper.persistenceFunctions.insertEntities(added)
        }
        if (modified.isNotEmpty()) {
            helper.persistenceFunctions.updateEntities(modified)
        }
        return LazyLinkedListOperatorHelper.Result(
            addedKeys = addedKeys,
            modifiedKeys = modifiedKeys
        )
    }
}
