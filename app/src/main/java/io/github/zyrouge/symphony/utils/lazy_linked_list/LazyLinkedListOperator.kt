package io.github.zyrouge.symphony.utils.lazy_linked_list

class LazyLinkedListOperator<K, V>(
    val entityFunctions: EntityFunctions<K, V>,
    val persistenceFunctions: PersistenceFunctions<K, V>,
    val dataChangeFunctions: DataChangeFunctions<K, V>? = null,
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

        suspend fun saveEntities(
            addedEntities: List<V>,
            modifiedEntities: List<V>,
            deletedKeys: List<K>,
        )
    }

    interface DataChangeFunctions<K, V> {
        fun onMarkedAsDeleted(key: K, value: V) {}
    }

    internal interface Operation<K, V> {
        suspend fun perform(): Changeset<K, V>
    }

    data class Changeset<K, V>(
        val headModified: Boolean = false,
        val addedKeys: List<K> = emptyList(),
        internal val addedEntities: List<V> = emptyList(),
        val modifiedKeys: List<K> = emptyList(),
        internal val modifiedEntities: List<V> = emptyList(),
        val deletedKeys: List<K> = emptyList(),
    )

    suspend fun <X> prependHead(
        values: List<X>,
        createFn: LazyLinkedListPrependHeadOperatorCreateFn<K, V, X>,
    ) = LazyLinkedListPrependHeadOperation(this, values, createFn).perform()

    suspend fun <X> append(
        insertAfterId: K?,
        values: List<X>,
        createFn: LazyLinkedListInsertAppendOperationCreateFn<K, V, X>,
    ) = LazyLinkedListInsertAppendOperation(this, insertAfterId, values, createFn).perform()

    suspend fun remove(keys: List<K>) = LazyLinkedListRemoveOperation(this, keys).perform()

    suspend fun persist(changeset: Changeset<K, V>) {
        persistenceFunctions.saveEntities(
            addedEntities = changeset.addedEntities,
            modifiedEntities = changeset.modifiedEntities,
            deletedKeys = changeset.deletedKeys,
        )
    }
}
