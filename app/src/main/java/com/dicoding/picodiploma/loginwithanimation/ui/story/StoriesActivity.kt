package com.dicoding.picodiploma.loginwithanimation.ui.story

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityStoriesBinding
import com.dicoding.picodiploma.loginwithanimation.ui.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.ui.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class StoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoriesBinding
    private lateinit var storiesViewModel: StoriesViewModel
    private lateinit var storiesAdapter: StoriesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storiesAdapter = StoriesAdapter()
        binding.rvStory.adapter = storiesAdapter

        storiesViewModel =
            ViewModelProvider(this, ViewModelFactory(this)).get(StoriesViewModel::class.java)

        binding.logoutNavigate.setOnClickListener {
            logoutUser()
        }

        binding.fabAddStory.setOnClickListener{
            val intent = Intent(this, AddStories::class.java)
            startActivity(intent)
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

            storiesViewModel.logout()

            val intent = Intent(this@StoriesActivity, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


}