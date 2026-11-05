package com.example.rekly.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.rekly.R
import com.example.rekly.databinding.ActivityMainBinding
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.util.SessionManager
import com.google.android.material.snackbar.Snackbar

/**
 * Central screen of the app: hosts the Summary, Subscriptions and Profile tabs, switched
 * by the bottom navigation, and the + button to add a subscription.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        applySystemBarInsets()
        setUpNavigation(savedInstanceState)
        binding.fabNewSubscription.setOnClickListener { openNewSubscription() }
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager(this).loggedUserId() == null) {
            startActivity(LoginActivity.newTask(this))
            finish()
        }
    }

    /**
     * After a rotation, the tab's fragment is recreated by the FragmentManager and the
     * bottom navigation restores the selected item here, after onCreate.
     * All that is left is to fix the title and the + button for the restored tab.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        updateHeader(binding.bottomNavigation.selectedItemId)
    }

    private fun setUpNavigation(savedInstanceState: Bundle?) {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        // Tapping the tab that is already open does not recreate the fragment
        binding.bottomNavigation.setOnItemReselectedListener { }

        // On first launch shows the Summary; after rotation the fragment was already restored
        if (savedInstanceState == null) {
            showTab(R.id.nav_summary)
        }
    }

    private fun showTab(@IdRes itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_subscriptions -> SubscriptionsFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> SummaryFragment()
        }
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment)
        }
        updateHeader(itemId)
    }

    /** Toolbar title according to the tab; the + button does not show on the Profile. */
    private fun updateHeader(@IdRes itemId: Int) {
        val title = when (itemId) {
            R.id.nav_subscriptions -> R.string.tab_subscriptions
            R.id.nav_profile -> R.string.tab_profile
            else -> R.string.tab_summary
        }
        supportActionBar?.setTitle(title)
        if (itemId == R.id.nav_profile) {
            binding.fabNewSubscription.hide()
        } else {
            binding.fabNewSubscription.show()
        }
    }

    // Replaced by opening the subscription form in task 6
    private fun openNewSubscription() {
        Snackbar.make(binding.root, R.string.notice_coming_soon, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.fabNewSubscription)
            .show()
    }

    /**
     * With the app drawing behind the system bars (edge-to-edge), each part handles
     * its own spacing: the toolbar extends its color up to the status bar, the bottom
     * navigation moves away from the navigation bar by itself and the sides
     * (phone in landscape) keep the full screen.
     */
    private fun applySystemBarInsets() {
        val type = WindowInsetsCompat.Type.systemBars()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(type)
            view.updatePadding(left = bars.left, right = bars.right)
            // Passes only the top and bottom to the children, since the sides were already handled
            WindowInsetsCompat.Builder(insets)
                .setInsets(type, Insets.of(0, bars.top, 0, bars.bottom))
                .build()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar.root) { view, insets ->
            view.updatePadding(top = insets.getInsets(type).top)
            insets
        }
    }

    companion object {
        fun newTask(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
