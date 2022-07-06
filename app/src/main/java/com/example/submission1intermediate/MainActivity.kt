package com.example.submission1intermediate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.submission1intermediate.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

class MainActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein by kodein()
    private lateinit var binding: ActivityMainBinding

    companion object{
        const val EXTRA_TOKEN = "extra_token"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navControl = navHostFragment.navController
        NavigationUI.setupWithNavController(navView, navControl)



    }





}