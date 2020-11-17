package com.example.android.navigationsamples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.android.navigationsamples.BackButtonBehaviour.POP_HOST_FRAGMENT
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavFragment : Fragment() {

    private val bottomNavSelectedItemIdKey = "BOTTOM_NAV_SELECTED_ITEM_ID_KEY"
    private var bottomNavSelectedItemId = R.id.home

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(bottomNavSelectedItemIdKey, bottomNavSelectedItemId)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            bottomNavSelectedItemId =
                savedInstanceState.getInt(bottomNavSelectedItemIdKey, bottomNavSelectedItemId)
        }
        setupBottomNavBar(view)
    }

    private fun setupBottomNavBar(view: View) {
        val bottomNavView = view.findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        val toolbar = view.findViewById<Toolbar>(R.id.bottom_nav_toolbar)
        // Your navGraphIds must have the same ids as your menuItem ids
        val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)

        addToolbarListener(toolbar)
        bottomNavView.selectedItemId = bottomNavSelectedItemId

        val controller = bottomNavView.setupWithNavController(
            fragmentManager = childFragmentManager,
            navGraphIds = navGraphIds,
            backButtonBehaviour = POP_HOST_FRAGMENT,
            containerId = R.id.bottom_nav_container,
            firstItemId = R.id.home,
            intent = requireActivity().intent
        )

        controller.observe(viewLifecycleOwner, { navController ->
            NavigationUI.setupWithNavController(toolbar, navController)
            bottomNavSelectedItemId = navController.graph.id
        })
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
