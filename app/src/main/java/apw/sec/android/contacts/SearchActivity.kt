package apw.sec.android.contacts;

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.database.*
import android.net.Uri
import android.os.*
import android.os.AsyncTask
import android.provider.*
import android.view.*
import android.content.pm.*
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.*
import androidx.appcompat.widget.SeslProgressBar
import androidx.recyclerview.widget.*
import com.google.android.material.snackbar.Snackbar
import dev.oneuiproject.oneui.layout.*
import dev.oneuiproject.oneui.utils.*
import androidx.indexscroll.widget.SeslIndexScrollView
import androidx.indexscroll.widget.*
import apw.sec.android.contacts.databinding.ActivitySearchBinding
import java.util.*
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


public class SearchActivity: AppCompatActivity() {

    private var _binding: ActivitySearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchAdapter
    private lateinit var data: ContactData
    private var contactList: List<Contact>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadContacts()
        val layoutManager: LinearLayoutManager = LinearLayoutManager(this@SearchActivity)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        val dividerItemDecoration: DividerItemDecoration = DividerItemDecoration(binding.recyclerView.context, layoutManager.getOrientation())
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
        binding.recyclerView.setHasFixedSize(true)
        binding.back.setOnClickListener{
            super.onBackPressed()
        }
        binding.input.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                if(s!!.isNotEmpty()) binding.clear.visibility = View.VISIBLE else binding.clear.visibility = View.GONE
                adapter.filter(s?.toString())
            }
            override fun afterTextChanged(s: Editable?){}
        })
        binding.clear.setOnClickListener{
            binding.input.text.clear()
        }
    }
    
    private inner class LoadContactsTask: AsyncTask<Void, Void, List<Contact>>(){
        
        override fun doInBackground(vararg voids: Void?): List<Contact>{
            val contacts: List<Contact> = contactList!!
            return contacts
        }
        
        override fun onPostExecute(contacts: List<Contact>?){
            if(contacts?.isNotEmpty()!!){
                adapter = SearchAdapter(applicationContext, contacts)
                binding.recyclerView.setAdapter(adapter)
                binding.recyclerView.setItemAnimator(null)
                binding.recyclerView.seslSetFillBottomEnabled(true)
                binding.recyclerView.seslSetLastRoundedCorner(true)
                binding.recyclerView.seslSetGoToTopEnabled(true)
                binding.recyclerView.seslSetSmoothScrollEnabled(true)
            } else{
                Toast.makeText(this@SearchActivity, "Error while loading contacts", Toast.LENGTH_LONG).show()
            }   
        }
    }
    
    protected fun loadContacts(){
        data = ContactData(this@SearchActivity)
        contactList = data.getContacts()
        LoadContactsTask().execute()
    }
    
    override fun onDestroy(){
        super.onDestroy()
        _binding = null
    }
}
