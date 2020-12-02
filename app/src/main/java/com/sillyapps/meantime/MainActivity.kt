package com.sillyapps.meantime

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.sillyapps.meantime.databinding.ActivityMainBinding
import com.sillyapps.meantime.ui.mainscreen.MainScreenFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDarkThemeIfNeeded()

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        binding.navView.setNavigationItemSelectedListener(this)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nightModeSwitcher -> {
                val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val appNightModeEnabled = preferences.getBoolean(PreferencesKeys.NIGHT_MODE_IS_ON, false)
                preferences.edit().putBoolean(PreferencesKeys.NIGHT_MODE_IS_ON, !appNightModeEnabled).apply()

                if (appNightModeEnabled)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                return false
            }

            R.id.mainScreenFragment -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.mainScreenFragment)
                return true
            }

            R.id.explorerFragment -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.explorerFragment)
                return true
            }

            R.id.schemeFragment -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.schemeFragment)
                return true
            }
        }
        return false
    }
}