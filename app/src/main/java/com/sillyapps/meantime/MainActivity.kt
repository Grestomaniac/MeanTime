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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.sillyapps.meantime.databinding.ActivityMainBinding
import com.sillyapps.meantime.ui.TimePickerItem
import com.sillyapps.meantime.utils.setDarkThemeIfNeeded
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDarkThemeIfNeeded()

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        drawerLayout = binding.drawerLayout

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        navView = binding.navView
        navView.setNavigationItemSelectedListener(this)
        navController.addOnDestinationChangedListener(this)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
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
                findNavController(R.id.nav_host_fragment).navigate(R.id.mainScreenFragment)
            }

            R.id.explorerFragment -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.explorerFragment)
            }

            R.id.schemeFragment -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.schemeFragment)
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

fun AppCompatActivity.hideKeyBoard(v: View) {
    val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(v.windowToken, 0)
}