package com.dicoding.picodiploma.loginwithanimation.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityHomeBinding
import com.dicoding.picodiploma.loginwithanimation.ui.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.ui.story.AddStories
import com.dicoding.picodiploma.loginwithanimation.ui.story.StoriesAdapter
import com.dicoding.picodiploma.loginwithanimation.ui.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var storiesAdapter: StoriesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvStory.layoutManager = LinearLayoutManager(this)
        storiesAdapter = StoriesAdapter()
        binding.rvStory.adapter = storiesAdapter



        homeViewModel =
            ViewModelProvider(this, ViewModelFactory(this)).get(HomeViewModel::class.java)

        binding.logoutNavigate.setOnClickListener {
            logoutUser()
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStories::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            homeViewModel.pagedStories.collectLatest { pagingData ->
                storiesAdapter.submitData(pagingData)
            }
        }


        storiesAdapter.addLoadStateListener { loadState ->
            when {
                loadState.refresh is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                loadState.refresh is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Error: ${(loadState.refresh as LoadState.Error).error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.logout, menu)
        return true
    }

    private suspend fun getTokenFromPreference(): String {
        val userPreference = UserPreference.getInstance(applicationContext.dataStore)
        return userPreference.getToken() ?: ""
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            val userPreference = UserPreference.getInstance(applicationContext.dataStore)
            userPreference.clearToken()

            homeViewModel.logout()

            val intent = Intent(this@HomeActivity, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}