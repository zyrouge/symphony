package io.github.zyrouge.symphony.utils.lazy_linked_list

typealias LazyLinkedListPrependHeadOperatorCreateFn<K, V, X> = (value: X, isHead: Boolean, nextId: K?) -> V

class LazyLinkedListPrependHeadOperation<K, V, X>(
    val operator: LazyLinkedListOperator<K, V>,
    val values: List<X>,
    val createFn: LazyLinkedListPrependHeadOperatorCreateFn<K, V, X>,
) : LazyLinkedListOperator.Operation<K, V> {
    override suspend fun perform(): LazyLinkedListOperator.Changeset<K, V> {
        if (values.isEmpty()) {
            return LazyLinkedListOperator.Changeset()
        }
        val addedKeys = mutableListOf<K>()
        val added = mutableListOf<V>()
        val modifiedKeys = mutableListOf<K>()
        val modified = mutableListOf<V>()
        val headEntity = operator.persistenceFunctions.getHeadEntity()
        var nextId = headEntity?.let { operator.entityFunctions.getEntityId(it) }
        for (i in (values.size - 1) downTo 0) {
            val value = values[i]
            val isHead = i == 0
            val entity = createFn(value, isHead, nextId)
            val id = operator.entityFunctions.getEntityId(entity)
            addedKeys.add(id)
            added.add(entity)
            nextId = id
        }
        if (headEntity != null) {
            val headEntityId = operator.entityFunctions.getEntityId(headEntity)
            val nHeadEntity = operator.entityFunctions.updateEntityIsHead(headEntity, false)
            modifiedKeys.add(headEntityId)
            modified.add(nHeadEntity)
        }
        return LazyLinkedListOperator.Changeset(
            headModified = true,
            addedKeys = addedKeys,
            addedEntities = added,
            modifiedKeys = modifiedKeys,
            modifiedEntities = modified,
        )
    }
}
