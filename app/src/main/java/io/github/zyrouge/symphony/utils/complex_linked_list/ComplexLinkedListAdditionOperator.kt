package io.github.zyrouge.symphony.utils.complex_linked_list

typealias ComplexLinkedListAdditionOperatorCreateFn<K, V, X> = (value: X, isHead: Boolean, nextId: K?) -> V

class ComplexLinkedListAdditionOperator<K, V, X>(
    val helper: ComplexLinkedListOperator<K, V>,
    val insertAtId: K?,
    val values: List<X>,
    val createFn: ComplexLinkedListAdditionOperatorCreateFn<K, V, X>,
) {
    data class Result<K>(val addedKeys: List<K>)

    suspend fun operate(): Result<K> {
        val headEntity = helper.persistenceFunctions.getHeadEntity()
        val tailEntity = insertAtId?.let { helper.persistenceFunctions.getEntity(it) }
            ?: helper.persistenceFunctions.getTailEntity()
        val addedKeys = mutableListOf<K>()
        val added = mutableListOf<V>()
        var nextId = tailEntity?.let { helper.entityFunctions.getEntityNextId(it) }
        val count = values.size
        for (i in (count - 1) downTo 0) {
            val value = values[i]
            val isHead = i == 0 && headEntity == null
            val entity = createFn(value, isHead, nextId)
            val id = helper.entityFunctions.getEntityId(entity)
            addedKeys.add(id)
            added.add(entity)
            nextId = id
        }
        helper.persistenceFunctions.insertEntities(added)
        return Result(addedKeys = addedKeys)
    }
}