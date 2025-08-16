package io.github.zyrouge.symphony.utils.lazy_linked_list

class LazyLinkedListRemoveOperator<K, V>(
    val helper: LazyLinkedListOperatorHelper<K, V>,
    val keys: List<K>,
) {
    suspend fun operate(): LazyLinkedListOperatorHelper.Result<K> {
        if (keys.isEmpty()) {
            return LazyLinkedListOperatorHelper.Result()
        }
        val entities = helper.persistenceFunctions.getEntitiesByIds(keys).toMutableMap()
        val idToPreviousId = mutableMapOf<K, K>()
        for (x in entities.values) {
            val id = helper.entityFunctions.getEntityId(x)
            val nextId = helper.entityFunctions.getEntityId(x)
            idToPreviousId[nextId] = id
        }
        for (x in helper.persistenceFunctions.getEntitiesByNextIds(keys).values) {
            val id = helper.entityFunctions.getEntityId(x)
            val nextId = helper.entityFunctions.getEntityId(x)
            entities.put(id, x)
            idToPreviousId.put(nextId, id)
        }
        val modified = mutableSetOf<K>()
        val deleted = mutableSetOf<K>()
        var headModified = false
        for (id in keys) {
            val entity = entities[id] ?: continue
            val isHead = helper.entityFunctions.getEntityIsHead(entity)
            if (isHead) {
                val nextId = helper.entityFunctions.getEntityNextId(entity) ?: continue
                val nextEntity = entities[nextId] ?: continue
                val nNextEntity = helper.entityFunctions.updateEntityIsHead(nextEntity, true)
                entities.put(nextId, nNextEntity)
                entities.remove(id)
                modified.add(nextId)
                modified.remove(id)
                deleted.add(id)
                headModified = true
                continue
            }
            val previousId = idToPreviousId[id] ?: continue
            val previousEntity = entities[previousId] ?: continue
            val nextId = helper.entityFunctions.getEntityNextId(entity)
            val nPreviousEntity = helper.entityFunctions.updateEntityNextId(previousEntity, nextId)
            entities.put(previousId, nPreviousEntity)
            entities.remove(id)
            modified.add(previousId)
            modified.remove(id)
            deleted.add(id)
        }
        if (modified.isNotEmpty()) {
            val modifiedEntities = modified.mapNotNull { entities[it] }.toList()
            helper.persistenceFunctions.updateEntities(modifiedEntities)
        }
        if (deleted.isNotEmpty()) {
            helper.persistenceFunctions.deleteEntities(deleted.toList())
        }
        return LazyLinkedListOperatorHelper.Result(
            headModified = headModified,
            modifiedKeys = modified.toList(),
            deletedKeys = deleted.toList(),
        )
    }
}