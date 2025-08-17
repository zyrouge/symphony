package io.github.zyrouge.symphony.utils.lazy_linked_list

class LazyLinkedListOperatorHelper<K, V>(
    val entityFunctions: EntityFunctions<K, V>,
    val persistenceFunctions: PersistenceFunctions<K, V>,
) {
    interface EntityFunctions<K, V> {
        fun getEntityId(entity: V): K
        fun getEntityNextId(entity: V): K?
        fun getEntityIsHead(entity: V): Boolean
        fun updateEntityNextId(entity: V, nNextId: K?): V
        fun updateEntityIsHead(entity: V, nIsHead: Boolean): V
    }

    interface PersistenceFunctions<K, V> {
        fun getEntitiesByIds(ids: List<K>): Map<K, V>
        fun getEntity(id: K) = getEntitiesByIds(listOf(id))[id]
        fun getEntitiesByNextIds(nextIds: List<K>): Map<K, V>
        fun getEntityByNextId(nextId: K) = getEntitiesByIds(listOf(nextId))[nextId]
        fun getHeadEntity(): V?
        fun getTailEntity(): V?
        suspend fun insertEntities(entities: List<V>)
        suspend fun updateEntities(entities: List<V>)
        suspend fun deleteEntities(ids: List<K>)
    }

    data class Result<K>(
        val headModified: Boolean = false,
        val addedKeys: List<K> = emptyList(),
        val modifiedKeys: List<K> = emptyList(),
        val deletedKeys: List<K> = emptyList(),
    )

    suspend fun <X> prependHead(
        values: List<X>,
        createFn: LazyLinkedListPrependHeadOperatorCreateFn<K, V, X>,
    ) = LazyLinkedListPrependMoveOperator(this, values, createFn).operate()

    suspend fun <X> append(
        insertAfterId: K?,
        values: List<X>,
        createFn: LazyLinkedListInsertAppendOperatorCreateFn<K, V, X>,
    ) = LazyLinkedListInsertAppendOperator(this, insertAfterId, values, createFn).operate()

    suspend fun remove(keys: List<K>) = LazyLinkedListRemoveOperator(this, keys).operate()
}