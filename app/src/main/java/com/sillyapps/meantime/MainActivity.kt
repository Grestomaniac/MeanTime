package com.sillyapps.meantime

import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.sillyapps.meantime.databinding.ActivityMainBinding
import com.sillyapps.meantime.ui.TimePickerItem
import com.sillyapps.meantime.utils.setDarkThemeIfNeeded
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener {
    lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDarkThemeIfNeeded()
        Timber.d("Activity created")

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        Timber.d("Activity inflated")
        drawerLayout = binding.drawerLayout

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navView = binding.navView
        navView.setNavigationItemSelectedListener(this)
        navController.addOnDestinationChangedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (navView.checkedItem == item) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return false
        }
        when (item.itemId) {
            R.id.nightModeSwitcher -> {
                val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                    applicationContext
                )
                val appNightModeEnabled = preferences.getBoolean(
                    PreferencesKeys.NIGHT_MODE_IS_ON,
                    false
                )
                preferences.edit().putBoolean(
                    PreferencesKeys.NIGHT_MODE_IS_ON,
                    !appNightModeEnabled
                ).apply()

                if (appNightModeEnabled)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                drawerLayout.closeDrawer(GravityCompat.START)
                return false
            }

            R.id.mainScreenFragment -> {
                navController.navigate(R.id.mainScreenFragment)
            }

            R.id.explorerFragment -> {
                navController.navigate(R.id.explorerFragment)
            }

            R.id.schemeFragment -> {
                navController.navigate(R.id.schemeFragment)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        navView.setCheckedItem(destination.id)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            if (it.action == MotionEvent.ACTION_DOWN) {
                val v: View? = currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(it.rawX.toInt(), it.rawY.toInt())) {
                        if (v.parent is TimePickerItem) {
                            (v.parent as TimePickerItem).onFocusChange(v, false)
                        }
                        v.clearFocus()
                        hideKeyBoard(v)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}

fun Fragment.setupToolbar(toolbar: Toolbar) {
    (requireActivity() as MainActivity).apply {
        val navController = findNavController()

        toolbar.setupWithNavController(navController, drawerLayout)

        setSupportActionBar(toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }
}

fun AppCompatActivity.hideKeyBoard(v: View) {
    val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(v.windowToken, 0)
}