package com.getir.patika.foodcouriers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        tabLayout = findViewById(R.id.tab_account)
        viewPager2 = findViewById(R.id.viewpager_account)
        pagerAdapter = PagerAdapter(supportFragmentManager,lifecycle).apply {
            addFragment(CreateAccountFragment())
            addFragment(LoginAccountFragment())
        }
        viewPager2.adapter = pagerAdapter

        val fab: FloatingActionButton = findViewById(R.id.fab_open_map)
        fab.setOnClickListener {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.action_createAccountFragment_to_setLocation2)
            fab.visibility = View.GONE
        }

        TabLayoutMediator(tabLayout,viewPager2){ tab, position ->
             when(position) {
                 0 -> {
                     tab.text = "Create Account"
                 }
                 1 -> {
                     tab.text = "Login Account"
                 }
             }

        }.attach()

    }
}