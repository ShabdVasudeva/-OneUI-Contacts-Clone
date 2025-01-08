package apw.sec.android.contacts;

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.*
import android.provider.*
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.*
import java.util.*
import apw.sec.android.contacts.databinding.ActivityInfoBinding

class InfoActivity: AppCompatActivity() {
    
    private var _binding: ActivityInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var contact: Contact
    private val random: Random = Random()
    private val messages = arrayOf<String>(
        "Hello, how are you?",
        "Have a great day!",
        "Stay positive!",
        "Keep going!",
        "You're doing great!",
        "Believe in yourself!"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contact = intent.getSerializableExtra("contact") as Contact
        binding.toolbar.setNavigationButtonAsBack()
        contact.name.let{
            binding.toolbar.setTitle("${it}")
            binding.name.text = it
        }
        updateBottomNavIcon()
        if (contact.photoUri != null) {
            Glide.with(this@InfoActivity)
                .load(contact.photoUri)
                .apply(RequestOptions().transform(RoundedCorners(1000)))
                .placeholder(R.drawable.adapter_item)
                .into(binding.photo)
        } else {
            binding.photo.setImageResource(R.drawable.adapter_item)
        }
        binding.number.text = contact.phoneNumber ?: "No number found"
        val mail: String = contact.email ?: "No email found"
        binding.mailText.text = mail
        binding.bottomNavigation.setOnNavigationItemSelectedListener{ item->
            when(item.itemId){
                R.id.edit ->{
                    val cursor = contentResolver.query(
                        Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(contact.phoneNumber ?: "No number found")
                        ),
                        arrayOf(ContactsContract.PhoneLookup._ID),
                        null,
                        null,
                        null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
                            val intent = Intent(Intent.ACTION_EDIT).apply {
                                data = contactUri
                                putExtra("finishActivityOnSaveCompleted", true)
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                R.id.share ->{
                    val email: String = contact.email ?: "No email found"
                    val intent: Intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Information")
                    intent.putExtra(Intent.EXTRA_TEXT, "Name: " + contact.name + "\nPhone: " + contact.phoneNumber + "\nEmail: " + email)
                    startActivity(Intent.createChooser(intent, "Send contact to..."))
                    true
                }
                R.id.fav ->{
                    if(contact.isStarred == true){
                        unstarRawContact(contact.id)
                    } else if(contact.isStarred == false){
                        starRawContact(contact.id)
                    }
                    updateBottomNavIcon()
                    true
                }
                R.id.mail ->{
                    composeEmail(contact.email)
                    true
                }
                else -> false
            }
        }
        binding.video.setOnClickListener{
            startWhatsApp(contact.phoneNumber ?: "No number found")
        }
        binding.call.setOnClickListener{
            startCall(contact.phoneNumber)
        }
        binding.text.setOnClickListener{
            val index: Int = random.nextInt(messages.size)
            val message: String = messages[index]
            sendSMS(contact.phoneNumber, message)
        }
    }
    
    private fun starRawContact(contactId: String) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, 1)
        }
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
        val rowsUpdated = contentResolver.update(uri, values, null, null)
        if (rowsUpdated > 0) {
            contact.isStarred = true
            Toast.makeText(applicationContext, "Contact has been added to favourites", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Failed to add contact in favourites", Toast.LENGTH_LONG).show()
        }
    }

    private fun unstarRawContact(contactId: String) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, 0)
        }
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())
        val rowsUpdated = contentResolver.update(uri, values, null, null)
        if (rowsUpdated > 0) {
            contact.isStarred = false
            Toast.makeText(applicationContext, "Contact has been removed from favourites", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Failed to remove contact from favourites", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateBottomNavIcon() {
        val starIcon = if (contact?.isStarred == true) {
            dev.oneuiproject.oneui.R.drawable.ic_oui_star
        } else {
            dev.oneuiproject.oneui.R.drawable.ic_oui_star_outline
        }
        binding.bottomNavigation.menu.findItem(R.id.fav).icon = ContextCompat.getDrawable(this, starIcon)
    }
    
    private fun updateBottomNavIconOnClick() {
        val starIcon = if (contact?.isStarred == true) {
            dev.oneuiproject.oneui.R.drawable.ic_oui_star
        } else {
            dev.oneuiproject.oneui.R.drawable.ic_oui_star_outline
        }
        binding.bottomNavigation.menu.findItem(R.id.fav).icon = ContextCompat.getDrawable(this, starIcon)
    }
    
    private fun composeEmail(emailAddress: String?) {
        val intent: Intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        try {
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(emailAddress!!))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject here")
            intent.putExtra(Intent.EXTRA_TEXT, "Body of the email")
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (err: Exception) {
            Toast.makeText(this, "This contact may not have any email data saved.", Toast.LENGTH_SHORT).show();
        }
    }
    
    public fun startWhatsApp(phoneNumber: String) {
        try {
            val intent: Intent =  Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/" + phoneNumber)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private fun startCall(phoneNumber: String?) {
        val callIntent: Intent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:" + phoneNumber)
        if(phoneNumber != null){
            try {
                startActivity(callIntent);
            } catch (e: SecurityException) {
                Toast.makeText(this, "Permission to make calls is required", Toast.LENGTH_SHORT).show()
            }
        } else{
            Toast.makeText(this, "No number available to call user", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteContact(rawContactId: Long) {
        try {
            val rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId)
            val rowsDeleted = contentResolver.delete(rawContactUri, null, null)
            if (rowsDeleted > 0) {
                Toast.makeText(
                    applicationContext,
                    "Contact removed successfully. Changes may appear after reopening the contacts app.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Failed to remove contact. Please check if the ID is valid.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("DeleteContact", "Error deleting contact by rawContactId: ", e)
            Toast.makeText(applicationContext, "An error occurred while deleting the contact.", Toast.LENGTH_LONG).show()
        }
    }
    private fun getRawContactIdByName(contactName: String): Long? {
        val cursor = contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID),
            "${ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY} = ?",
            arrayOf(contactName),
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(ContactsContract.RawContacts._ID))
            }
        }
        return null
    }
    
    public fun sendSMS(phoneNumber: String?, message: String) {
        val smsIntent: Intent = Intent(Intent.ACTION_SENDTO)
        smsIntent.data = Uri.parse("smsto:" + phoneNumber)
        smsIntent.putExtra("sms_body", message)
        if(phoneNumber != null){
            try {
                startActivity(smsIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "No messaging app found", Toast.LENGTH_SHORT).show()
            }
        } else{
            Toast.makeText(this, "No phone number found to send text to the user", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun copyToClip(ctxt: Context, text: String){
        val clipBoard = ctxt.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("number", text)
        clipBoard.setPrimaryClip(clip)
        Toast.makeText(applicationContext, "Number has been copied to cliboard", Toast.LENGTH_SHORT).show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        getMenuInflater().inflate(R.menu.info_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.getItemId()){
            R.id.delete ->{
                deleteContact(getRawContactIdByName(contact.name)!!)
                return true
            }
            R.id.copy ->{
                val str: String = contact.phoneNumber ?: "No number Found"
                copyToClip(this,str)
                return true
            }
        }
        return false
    }
    
    override fun onDestroy(){
        super.onDestroy()
        _binding = null
    }
}