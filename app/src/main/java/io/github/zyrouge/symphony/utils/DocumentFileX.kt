package io.github.zyrouge.symphony.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract

// Source:  https://android.googlesource.com/platform/frameworks/support/+/a9ac247af2afd4115c3eb6d16c05bc92737d6305/documentfile/src/main/java/androidx/documentfile/provider
data class DocumentFileX(
    private val context: Context,
    val id: String,
    val name: String,
    val mimeType: String,
    val lastModified: Long,
    val size: Long,
    val uri: Uri,
) {
    val isDirectory: Boolean
        get() = mimeType.contentEquals(DocumentsContract.Document.MIME_TYPE_DIR)

    fun list(onChild: (file: DocumentFileX) -> Unit) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            uri,
            DocumentsContract.getDocumentId(uri)
        )
        context.contentResolver.query(childrenUri, selectionColumns, null, null, null)?.use {
            while (it.moveToNext()) {
                val file = fromCursor(context, it) { id ->
                    DocumentsContract.buildDocumentUriUsingTree(uri, id)
                }
                onChild(file)
            }
        }
    }

    companion object {
        val selectionColumns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
        )

        private fun fromCursor(
            context: Context,
            cursor: Cursor,
            uriFactory: (String) -> Uri,
        ): DocumentFileX {
            val id = cursor.getString(0)
            return DocumentFileX(
                context = context,
                id = id,
                name = cursor.getString(1),
                mimeType = cursor.getString(2),
                lastModified = cursor.getLong(3),
                size = cursor.getLong(4),
                uri = uriFactory(id),
            )
        }

        fun fromSingleUri(context: Context, uri: Uri) = context.contentResolver
            .query(uri, selectionColumns, null, null, null)
            ?.use {
                when {
                    it.moveToNext() -> fromCursor(context, it) { _ -> uri }
                    else -> null
                }
            }

        fun fromTreeUri(context: Context, treeUri: Uri) = fromSingleUri(
            context,
            DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
        )
    }
}