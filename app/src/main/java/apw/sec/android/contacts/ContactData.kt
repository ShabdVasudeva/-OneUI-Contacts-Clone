package apw.sec.android.contacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract

class ContactData(private val context: Context) {
    fun getContacts(): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val contentResolver: ContentResolver = context.contentResolver

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Contacts.STARRED,
                ContactsContract.CommonDataKinds.Photo.PHOTO_URI
            ),
            "${ContactsContract.Data.MIMETYPE} IN (?, ?)",
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            ),
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )

        cursor?.let {
            val contactMap = mutableMapOf<String, Contact>() // Use a map to group data by contact ID
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""
                val mimeType = it.getString(it.getColumnIndex(ContactsContract.Data.MIMETYPE))
                val isStarred = it.getString(it.getColumnIndex(ContactsContract.Contacts.STARRED)) == "1"
                val photoUri = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))
                val rawId = it.getString(it.getColumnIndex(ContactsContract.RawContacts._ID)) ?: ""

                // Get or create the contact object
                val contact = contactMap[contactId] ?: Contact(
                    id = contactId,
                    name = name,
                    phoneNumber = null,
                    photoUri = photoUri,
                    email = null,
                    isStarred = isStarred,
                    rawId = rawId
                )

                // Update the phone number or email based on mimeType
                if (mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                    contact.phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                } else if (mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
                    contact.email = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                }

                // Update the map
                contactMap[contactId] = contact
            }
            contactList.addAll(contactMap.values) // Add all contacts to the list
            it.close()
        }
        return contactList
    }
}