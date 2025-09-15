package io.github.zyrouge.symphony.utils.lazy_linked_list

class LazyLinkedListRemoveOperation<K, V>(
    val operator: LazyLinkedListOperator<K, V>,
    val keys: List<K>,
) : LazyLinkedListOperator.Operation<K, V> {
    override suspend fun perform(): LazyLinkedListOperator.Changeset<K, V> {
        if (keys.isEmpty()) {
            return LazyLinkedListOperator.Changeset()
        }
        val entities = operator.persistenceFunctions.getEntitiesByIds(keys).toMutableMap()
        val idToPreviousId = mutableMapOf<K, K>()
        for (x in entities.values) {
            val id = operator.entityFunctions.getEntityId(x)
            val nextId = operator.entityFunctions.getEntityId(x)
            idToPreviousId[nextId] = id
        }
        for (x in operator.persistenceFunctions.getEntitiesByNextIds(keys).values) {
            val id = operator.entityFunctions.getEntityId(x)
            val nextId = operator.entityFunctions.getEntityId(x)
            entities.put(id, x)
            idToPreviousId.put(nextId, id)
        }
        val modified = mutableSetOf<K>()
        val deleted = mutableSetOf<K>()
        var headModified = false
        for (id in keys) {
            val entity = entities[id] ?: continue
            val isHead = operator.entityFunctions.getEntityIsHead(entity)
            if (isHead) {
                val nextId = operator.entityFunctions.getEntityNextId(entity) ?: continue
                val nextEntity = entities[nextId] ?: continue
                val nNextEntity = operator.entityFunctions.updateEntityIsHead(nextEntity, true)
                entities.put(nextId, nNextEntity)
                entities.remove(id)
                modified.add(nextId)
                modified.remove(id)
                deleted.add(id)
                headModified = true
                operator.dataChangeFunctions?.onMarkedAsDeleted(id, entity)
                continue
            }
            val previousId = idToPreviousId[id] ?: continue
            val previousEntity = entities[previousId] ?: continue
            val nextId = operator.entityFunctions.getEntityNextId(entity)
            val nPreviousEntity =
                operator.entityFunctions.updateEntityNextId(previousEntity, nextId)
            entities.put(previousId, nPreviousEntity)
            entities.remove(id)
            modified.add(previousId)
            modified.remove(id)
            deleted.add(id)
            operator.dataChangeFunctions?.onMarkedAsDeleted(id, entity)
        }
        val modifiedEntities = modified.mapNotNull { entities[it] }.toList()
        return LazyLinkedListOperator.Changeset(
            headModified = headModified,
            modifiedKeys = modified.toList(),
            modifiedEntities = modifiedEntities,
            deletedKeys = deleted.toList(),
        )
    }
}