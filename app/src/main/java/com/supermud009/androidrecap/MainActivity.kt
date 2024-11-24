package com.supermud009.androidrecap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.supermud009.androidrecap.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activationButton.setOnClickListener {
            viewModel.getData()
            Snackbar.make(
                binding.root,
                "Activating",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        lifecycleScope.launchWhenStarted {
           repeatOnLifecycle(Lifecycle.State.STARTED) {
               viewModel.uiState.collect { stateValue ->
                   binding.textValue.text = stateValue
               }
           }
        }
    }
}

class MainActivityViewModel: ViewModel() {
    private val _uiState = MutableStateFlow("Hello world")
    val uiState = _uiState

    fun getData() {
        viewModelScope.launch {
            Log.e(this::class.java.simpleName, "Setting state...")
            _uiState.value = "Magic"
            delay(5_000)
            _uiState.value = "Super Magic"
        }
    }
}
