package apw.sec.android.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import android.content.*
import android.widget.*
import android.net.Uri
import android.provider.Settings
import apw.sec.android.contacts.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private var _binding: ActivitySettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationButtonAsBack()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.place_holder, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
    
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            val channel: Preference? = findPreference("channel")
            channel?.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/AndroidPortWorld"))
                try {
                    startActivity(intent)
                } catch (err: Exception) {
                    activity?.applicationContext?.let { context ->
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
                    }
                }
                true
            }
            val developer: Preference? = findPreference("shabd")
            developer?.setOnPreferenceClickListener{
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ShabdVasudeva"))
                try{
                    startActivity(intent)
                } catch(err: Exception){
                    activity?.applicationContext?.let { context ->
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
                    }
                }
                true
            }
            val perm: Preference? = findPreference("perm")
            perm?.setOnPreferenceClickListener{
                activity?.applicationContext?.let{context ->
                    val intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                }
                true
            }
            val abt: Preference? = findPreference("abt")
            abt?.setOnPreferenceClickListener{
                activity?.applicationContext?.let{context ->
                    val intent: Intent = Intent(context, AboutActivity::class.java)
                    startActivity(intent)
                }
                true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}