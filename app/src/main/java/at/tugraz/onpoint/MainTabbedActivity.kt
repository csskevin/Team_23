package at.tugraz.onpoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import at.tugraz.onpoint.database.getDbInstance
import at.tugraz.onpoint.moodle.API
import at.tugraz.onpoint.moodle.LoginErrorData
import at.tugraz.onpoint.moodle.LoginSuccessData
import at.tugraz.onpoint.ui.main.SectionsPagerAdapter
import at.tugraz.onpoint.ui.main.TAB_INDEX_MAIN
import com.google.android.material.tabs.TabLayout

class MainTabbedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Instantiation of the singleton DB once globally so it can be
        // available in all other tabs
        getDbInstance(this)
        // Preparation of the notification channels used throughout the app
        createNotificationChannel()
        // Languages
        val languagehandler = LanguageHandler()
        languagehandler.loadLocale(baseContext)
        // Display activity and tabs
        setContentView(R.layout.activity_maintabbed)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        findViewById<Button>(R.id.switch_language).setOnClickListener { onLanguageSwitch() }
        findViewById<Button>(R.id.test_moodle_request).setOnClickListener { moodleTest() }
        ////////////////////////////////////////////////////////////////////////////////////////////
        /// source: https://proandroiddev.com/easy-approach-to-navigation-drawer-7fe87d8fd7e7
        setSupportActionBar(findViewById(R.id.toolbar))
        val sidebar = findViewById<DrawerLayout>(R.id.sidebar)
        val sidebarToggle = ActionBarDrawerToggle(this, sidebar, R.string.open, R.string.close)
        sidebar.addDrawerListener(sidebarToggle)
        sidebarToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Switch to proper tab, if an intent requested it. Otherwise open the default tab.
        val tabToOpen = intent.getIntExtra("tabToOpen", TAB_INDEX_MAIN)
        viewPager.currentItem = tabToOpen
    }

    fun moodleTest() {
        val moodle_api = API()
        moodle_api.login("kevin", "123") {
            data: Any -> when(data) {
                is LoginSuccessData -> {
                    println("Login was successful")
                }
                is LoginErrorData -> {
                    println("Login failed")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sidebar = findViewById<DrawerLayout>(R.id.sidebar)
        return when (item.itemId) {
            android.R.id.home -> {
                sidebar.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val sidebar = findViewById<DrawerLayout>(R.id.sidebar)
        if (sidebar.isDrawerOpen(GravityCompat.START)) {
            sidebar.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /// source: https://proandroiddev.com/easy-approach-to-navigation-drawer-7fe87d8fd7e7
    ////////////////////////////////////////////////////////////////////////////////////////////////


    fun onLanguageSwitch() {
        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val currentLocal: String = sharedPref.getString("locale_to_set", "")!!
        val languagehandler = LanguageHandler()
        if(currentLocal == "en") {
            languagehandler.setLanguageToSettings(baseContext, "zh")
        }
        if(currentLocal == "zh") {
            languagehandler.setLanguageToSettings(baseContext, "en")
        }
        recreate()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.CHANNEL_ID), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
