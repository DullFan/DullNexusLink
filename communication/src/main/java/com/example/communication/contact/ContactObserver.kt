package com.example.communication.contact

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import com.example.communication.datastore.LastUpdateTimeManager.getContactTime
import com.example.communication.datastore.LastUpdateTimeManager.updateContactTime
import com.example.communication.room.entity.ContactPersonEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 联系人观察者
 */
class ContactObserver(
    private val context: Context,
    private val queryManager: ContactQueryManager,
    private val onContactsChanged: suspend (List<ContactPersonEntity>) -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onChange(selfChange: Boolean) {
        scope.launch {
            val currentLastUpdateTime = context.getContactTime()
            val contacts = queryManager.queryContacts(
                selection = "${ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP} > ?",
                selectionArgs = arrayOf(currentLastUpdateTime.toString())
            )
            onContactsChanged(contacts)
            context.updateContactTime()
        }
    }

    fun release() {
        scope.cancel()
    }
}
