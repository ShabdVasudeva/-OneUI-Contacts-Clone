package apw.sec.android.contacts;

import java.io.Serializable

data class Contact(
    val id: String,
    val name: String,
    var phoneNumber: String?,
    val photoUri: String?,
    var email: String?,
    var isStarred: Boolean,
    val rawId: String
): Serializable{}