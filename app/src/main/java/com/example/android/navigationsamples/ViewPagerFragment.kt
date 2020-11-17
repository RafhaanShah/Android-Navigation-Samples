package com.example.android.navigationsamples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter.FragmentTransactionCallback.OnPostEventListener
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ViewPagerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager(view)
    }

    // Needed to avoid memory leaks
    // see: https://stackoverflow.com/a/62861424
    // and: https://stackoverflow.com/a/62184494
    override fun onDestroy() {
        viewPager.adapter = null
        tabLayoutMediator.detach()
        super.onDestroy()
    }

    private fun setupViewPager(view: View) {
        val labels = listOf("Home", "Leaderboard", "Register")
        val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)

        val demoCollectionAdapter = NavHostFragmentAdapter(
            fragmentManager = childFragmentManager,
            lifecycle = viewLifecycleOwner.lifecycle,
            navGraphIds = navGraphIds
        )
        viewPager = view.findViewById(R.id.view_pager)
        viewPager.adapter = demoCollectionAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.view_pager_tab_layout)
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = (labels[position])
        }
        tabLayoutMediator.attach()

        addToolbarListener(view.findViewById(R.id.view_pager_toolbar))
    }

    private fun addToolbarListener(toolbar: Toolbar) {
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    findNavController().navigate(R.id.settings)
                    true
                }
                else -> false
            }
        }
    }

}

// Also see https://stackoverflow.com/a/64033693
class NavHostFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val navGraphIds: List<Int>
) : // Use this constructor to avoid this issue: https://stackoverflow.com/a/62184494
    FragmentStateAdapter(fragmentManager, lifecycle) {

    init {
        // Needs: "androidx.viewpager2:viewpager2:1.1.0-alpha01" or higher
        // Taken from: https://stackoverflow.com/a/62629996
        // Add a FragmentTransactionCallback to handle changing
        // the primary navigation fragment
        registerFragmentTransactionCallback(object : FragmentTransactionCallback() {
            override fun onFragmentMaxLifecyclePreUpdated(
                fragment: Fragment,
                maxLifecycleState: Lifecycle.State
            ) = if (maxLifecycleState == Lifecycle.State.RESUMED) {
                // This fragment is becoming the active Fragment - set it to
                // the primary navigation fragment in the OnPostEventListener
                OnPostEventListener {
                    fragment.parentFragmentManager.commitNow {
                        setPrimaryNavigationFragment(fragment)
                    }
                }
            } else {
                super.onFragmentMaxLifecyclePreUpdated(fragment, maxLifecycleState)
            }
        })
    }

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        return NavHostFragment.create(navGraphIds[position])
    }

    override fun getItemCount(): Int = navGraphIds.size

}
