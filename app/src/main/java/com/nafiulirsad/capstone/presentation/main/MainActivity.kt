package com.nafiulirsad.capstone.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nafiulirsad.capstone.R
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.databinding.ActivityMainBinding
import com.nafiulirsad.capstone.presentation.common.toNightMode
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupNavigation()
        observeTheme()
    }

    /** targetSdk 35+ draws edge to edge, so the system bars are padded manually. */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, windowInsets ->
            val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.isVisible = destination.id != R.id.detailFragment
        }
    }

    private fun observeTheme() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeMode.collect { themeMode ->
                    themeMode?.let(::applyTheme)
                }
            }
        }
    }

    /** No-op when the mode is already applied, so this never recreates the Activity twice. */
    private fun applyTheme(themeMode: ThemeMode) {
        val nightMode = themeMode.toNightMode()
        if (AppCompatDelegate.getDefaultNightMode() == nightMode) return

        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
