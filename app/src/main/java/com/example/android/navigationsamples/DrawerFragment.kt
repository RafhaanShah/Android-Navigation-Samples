package com.example.android.navigationsamples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView

class DrawerFragment : Fragment() {

    private val drawerSelectedItemIdKey = "DRAWER_SELECTED_ITEM_ID_KEY"
    private var drawerSelectedItemId = R.id.home // Must be your starting destination,
    // same as the 'checked' one in your menu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            drawerSelectedItemId = it.getInt(drawerSelectedItemIdKey, drawerSelectedItemId)
        }
        setupDrawer(view)
    }

    // Needed to maintain correct state over rotations
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(drawerSelectedItemIdKey, drawerSelectedItemId)
        super.onSaveInstanceState(outState)
    }

    private fun setupDrawer(view: View) {
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = view.findViewById<Toolbar>(R.id.drawer_toolbar)
        val navView = view.findViewById<NavigationView>(R.id.drawer_nav_view)
        // Your navGraphIds must have the same ids as your menuItem ids
        val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)

        val controller = navView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = childFragmentManager,
            containerId = R.id.drawer_container,
            currentItemId = drawerSelectedItemId,
            parentNavController = findNavController(), // Optional: only if you need to
            // navigate to external destinations
            intent = requireActivity().intent
        )

        controller.observe(viewLifecycleOwner, { navController ->
            NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)
            drawerSelectedItemId = navController.graph.id
        })
    }

}
