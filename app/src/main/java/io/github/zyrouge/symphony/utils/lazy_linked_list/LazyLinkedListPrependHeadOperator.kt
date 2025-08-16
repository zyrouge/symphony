package io.github.zyrouge.symphony.utils.lazy_linked_list

typealias LazyLinkedListPrependHeadOperatorCreateFn<K, V, X> = (value: X, isHead: Boolean, nextId: K?) -> V

class LazyLinkedListPrependHeadOperator<K, V, X>(
    val helper: LazyLinkedListOperatorHelper<K, V>,
    val values: List<X>,
    val createFn: LazyLinkedListPrependHeadOperatorCreateFn<K, V, X>,
) {
    suspend fun operate(): LazyLinkedListOperatorHelper.Result<K> {
        if (values.isEmpty()) {
            return LazyLinkedListOperatorHelper.Result()
        }
        val addedKeys = mutableListOf<K>()
        val added = mutableListOf<V>()
        val modifiedKeys = mutableListOf<K>()
        val modified = mutableListOf<V>()
        val headEntity = helper.persistenceFunctions.getHeadEntity()
        var nextId = headEntity?.let { helper.entityFunctions.getEntityId(it) }
        for (i in (values.size - 1) downTo 0) {
            val value = values[i]
            val isHead = i == 0
            val entity = createFn(value, isHead, nextId)
            val id = helper.entityFunctions.getEntityId(entity)
            addedKeys.add(id)
            added.add(entity)
            nextId = id
        }
        if (headEntity != null) {
            val headEntityId = helper.entityFunctions.getEntityId(headEntity)
            val nHeadEntity = helper.entityFunctions.updateEntityIsHead(headEntity, false)
            modifiedKeys.add(headEntityId)
            modified.add(nHeadEntity)
        }
        if (added.isNotEmpty()) {
            helper.persistenceFunctions.insertEntities(added)
        }
        if (modified.isNotEmpty()) {
            helper.persistenceFunctions.updateEntities(modified)
        }
        return LazyLinkedListOperatorHelper.Result(
            headModified = true,
            addedKeys = addedKeys,
            modifiedKeys = modifiedKeys
        )
    }
}
