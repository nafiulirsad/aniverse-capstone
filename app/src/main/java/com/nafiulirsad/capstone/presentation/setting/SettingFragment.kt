package com.nafiulirsad.capstone.presentation.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nafiulirsad.capstone.R
import com.nafiulirsad.capstone.core.domain.model.ThemeMode
import com.nafiulirsad.capstone.databinding.FragmentSettingBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: SettingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeSelector()
        observeThemeMode()
    }

    /**
     * Per-button click listeners rather than `RadioGroup.setOnCheckedChangeListener`: only a real
     * tap may persist a choice, so restoring the stored value can never write it back.
     */
    private fun setupThemeSelector() = with(binding) {
        radioThemeSystem.setOnClickListener { viewModel.onThemeModeSelected(ThemeMode.SYSTEM) }
        radioThemeLight.setOnClickListener { viewModel.onThemeModeSelected(ThemeMode.LIGHT) }
        radioThemeDark.setOnClickListener { viewModel.onThemeModeSelected(ThemeMode.DARK) }
    }

    private fun observeThemeMode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeMode.collect { themeMode ->
                    themeMode?.let(::renderSelection)
                }
            }
        }
    }

    private fun renderSelection(themeMode: ThemeMode) {
        val targetId = themeMode.toRadioId()
        if (binding.radioGroupTheme.checkedRadioButtonId == targetId) return

        binding.radioGroupTheme.check(targetId)
    }

    private fun ThemeMode.toRadioId(): Int = when (this) {
        ThemeMode.LIGHT -> R.id.radio_theme_light
        ThemeMode.DARK -> R.id.radio_theme_dark
        ThemeMode.SYSTEM -> R.id.radio_theme_system
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
