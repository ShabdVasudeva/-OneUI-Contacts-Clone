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
import java.util.*
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import apw.sec.android.contacts.databinding.ActivityStarredBinding

public class StarredActivity: AppCompatActivity() {
    
    private var _binding: ActivityStarredBinding? = null
    private val binding get() = _binding!!
    private var contactList: List<Contact>? = null
    private lateinit var data: ContactData
    private lateinit var adapter: StarredAdapter
    public lateinit var mIndexScrollView: SeslIndexScrollView
    private var mCurrentSectionIndex: Int = 0
    private var mIsTextModeEnabled: Boolean = true
    private var mIsIndexBarPressed: Boolean = false
    private var mHideIndexBar: Runnable = Runnable{
        IndexScrollUtils.animateVisibility(mIndexScrollView, false)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStarredBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationButtonAsBack()
        loadContacts()
        val layoutManager: LinearLayoutManager = LinearLayoutManager(this@StarredActivity)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        val dividerItemDecoration: DividerItemDecoration = DividerItemDecoration(binding.recyclerView.context, layoutManager.getOrientation())
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
        binding.recyclerView.setHasFixedSize(true)
        mIndexScrollView = binding.indexscrollView
        mIndexScrollView.setIndexBarTextMode(mIsTextModeEnabled)
        mIndexScrollView.invalidate()
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
        data = ContactData(this@StarredActivity)
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
                adapter = StarredAdapter(contacts, applicationContext)
                binding.toolbar.setExpandedSubtitle("Total contacts: ${adapter.getItemCount() ?: 0}")
                binding.recyclerView.setAdapter(adapter)
                binding.recyclerView.setItemAnimator(null)
                binding.recyclerView.seslSetFillBottomEnabled(true)
                binding.recyclerView.seslSetLastRoundedCorner(true)
                binding.recyclerView.seslSetGoToTopEnabled(true)
                binding.recyclerView.seslSetSmoothScrollEnabled(true)
                initIndexScroll()
            } else{
                Toast.makeText(this@StarredActivity, "Error while loading contacts", Toast.LENGTH_LONG).show()
            }   
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean{
        getMenuInflater().inflate(apw.sec.android.contacts.R.menu.star_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.getItemId()){
            R.id.refresh ->{
                val newData: ContactData = ContactData(this@StarredActivity)
                val newList: List<Contact> = newData.getContacts()
                adapter.updateContacts(newList)
                binding.toolbar.setExpandedSubtitle("Total contacts: ${adapter.getItemCount() ?: 0}")
                Toast.makeText(this@StarredActivity, "Contacts Has been updated", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.settings ->{
                startActivity(Intent(this@StarredActivity, SettingsActivity::class.java))
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
