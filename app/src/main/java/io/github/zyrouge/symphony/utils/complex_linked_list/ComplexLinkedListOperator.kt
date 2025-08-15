package io.github.zyrouge.symphony.utils.complex_linked_list

class ComplexLinkedListOperator<K, V>(
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

    suspend fun <X> add(
        insertAtId: K?,
        values: List<X>,
        createFn: ComplexLinkedListAdditionOperatorCreateFn<K, V, X>,
    ) = ComplexLinkedListAdditionOperator(this, insertAtId, values, createFn).operate()

    suspend fun remove(keys: List<K>) = ComplexLinkedListRemoveOperator(this, keys).operate()
}