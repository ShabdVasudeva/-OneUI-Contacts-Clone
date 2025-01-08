package apw.sec.android.contacts

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
import androidx.transition.*
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
import apw.sec.android.contacts.databinding.ActivityMainBinding
import java.util.*
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

public class MainActivity : AppCompatActivity() {
    
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var contactList: List<Contact>? = null
    private val PERMISSION_REQUEST_READ_CONTACTS: Int = 100
    private lateinit var adapter: ContactsAdapter
    private lateinit var data: ContactData
    public lateinit var mIndexScrollView: SeslIndexScrollView
    private var mCurrentSectionIndex: Int = 0
    private var mIsTextModeEnabled: Boolean = true
    private var mIsIndexBarPressed: Boolean = false
    private var mHideIndexBar: Runnable = Runnable{
        IndexScrollUtils.animateVisibility(mIndexScrollView, false)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationButtonAsBack()
        binding.recyclerView.visibility = View.GONE
        Handler().postDelayed({
            binding.progress.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }, 1600)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(this@MainActivity)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        val dividerItemDecoration: DividerItemDecoration = DividerItemDecoration(binding.recyclerView.context, layoutManager.getOrientation())
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
        binding.recyclerView.setHasFixedSize(true)
        if(hasPermissions()) requestPermissions() else loadContacts()
        mIndexScrollView = binding.indexscrollView
        mIndexScrollView.setIndexBarTextMode(mIsTextModeEnabled)
        mIndexScrollView.invalidate()
        binding.fab.setOnClickListener{
            Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int){
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0 && binding.fab.visibility == View.VISIBLE){
                    binding.fab.hide()
                } else if(dy < 0 && binding.fab.visibility == View.GONE){
                    binding.fab.show()
                }
            }
        })
        binding.fab.setOnClickListener{
            val intent: Intent = Intent(Intent.ACTION_INSERT)
            intent.type = ContactsContract.RawContacts.CONTENT_TYPE
            try {
                startActivity(intent)
            } catch (err: Exception) {
                Toast.makeText(this@MainActivity, "An error occured", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestPermissions(){
        ActivityCompat.requestPermissions(this, 
            arrayOf<String>(Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE
                    ),
                    PERMISSION_REQUEST_READ_CONTACTS)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                Toast.makeText(this, "Please Give contacts permission first", Toast.LENGTH_LONG)
                        .show()
            }
        }
    }
    
    private fun initIndexScroll() {
        val isRtl = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        mIndexScrollView.setIndexBarGravity(
            if (isRtl) SeslIndexScrollView.GRAVITY_INDEX_BAR_LEFT else SeslIndexScrollView.GRAVITY_INDEX_BAR_RIGHT
        )
        val sectionNames = mutableListOf<String>()
        val sectionPositions = mutableListOf<String>()
        contactList?.forEachIndexed { index, contact ->
            val name = contact.name.trim()
            if (name.isNotEmpty()) {
                val firstChar = name[0].uppercaseChar().toString()
                if (!sectionNames.contains(firstChar)) {
                    sectionNames.add(firstChar) 
                    sectionPositions.add(index.toString())
                }
            }
        }
        sectionNames.forEachIndexed { index, section ->
            val sectionPosition = sectionPositions[index]
            val indexer = SeslArrayIndexer(listOf(section), "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z") 
            mIndexScrollView.setIndexer(indexer)
        }
        mIndexScrollView.setOnIndexBarEventListener(object : SeslIndexScrollView.OnIndexBarEventListener {
            override fun onIndexChanged(sectionIndex: Int) {
                if (mCurrentSectionIndex != sectionIndex) {
                    mCurrentSectionIndex = sectionIndex
                    if (binding.recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                        binding.recyclerView.stopScroll()
                    }
                    val position = sectionPositions[sectionIndex].toInt()
                    (binding.recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)
                }
            }

            override fun onPressed(v: Float) {
                mIsIndexBarPressed = true
                binding.recyclerView.removeCallbacks(mHideIndexBar)
            }

            override fun onReleased(v: Float) {
                mIsIndexBarPressed = false
                if (binding.recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                binding.recyclerView.postDelayed(mHideIndexBar, 1500)
                }
            }
        })
        mIndexScrollView.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !mIsIndexBarPressed) {
                    recyclerView.postDelayed(mHideIndexBar, 1500)
                } else {
                    recyclerView.removeCallbacks(mHideIndexBar)
                    IndexScrollUtils.animateVisibility(mIndexScrollView, true)
                }
            }
        })
    }
    
    protected fun loadContacts(){
        data = ContactData(this@MainActivity)
        contactList = data.getContacts()
        LoadContactsTask().execute()
    }
    
    private inner class LoadContactsTask: AsyncTask<Void, Void, List<Contact>>(){
        
        override fun doInBackground(vararg voids: Void?): List<Contact>{
            val contacts: List<Contact> = contactList!!
            return contacts
        }
        
        override fun onPostExecute(contacts: List<Contact>?){
            if(contacts?.isNotEmpty()!!){
                adapter = ContactsAdapter(contacts, applicationContext)
                binding.toolbar.setExpandedSubtitle("Total contacts: ${adapter.getItemCount() ?: 0}")
                binding.recyclerView.setAdapter(adapter)
                binding.recyclerView.setItemAnimator(null)
                binding.recyclerView.seslSetFillBottomEnabled(true)
                binding.recyclerView.seslSetLastRoundedCorner(true)
                binding.recyclerView.seslSetIndexTipEnabled(true)
                binding.recyclerView.seslSetGoToTopEnabled(true)
                binding.recyclerView.seslSetSmoothScrollEnabled(true)
                initIndexScroll()
            } else{
                Toast.makeText(this@MainActivity, "Error while loading contacts", Toast.LENGTH_LONG).show()
            }   
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        getMenuInflater().inflate(apw.sec.android.contacts.R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.getItemId()){
            R.id.refresh ->{
                binding.recyclerView.setVisibility(View.GONE)
                binding.progress.setVisibility(View.VISIBLE)
                val newData: ContactData = ContactData(this@MainActivity)
                val newList: List<Contact> = newData.getContacts()
                adapter.updateContacts(newList)
                Handler().postDelayed(Runnable{
                    binding.progress.setVisibility(View.GONE)
                    binding.indexscrollView.setVisibility(View.GONE)
                    binding.recyclerView.setVisibility(View.VISIBLE)
                }, 1600)
                Toast.makeText(this@MainActivity, "Contacts Has been updated", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.starred ->{
                startActivity(Intent(this@MainActivity, StarredActivity::class.java))
                return true
            }
            R.id.search ->{
                startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                return true
            }
            R.id.settings ->{
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
        }
        return false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}