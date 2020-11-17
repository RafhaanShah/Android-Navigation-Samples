package com.example.android.navigationsamples

import android.content.Intent
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import android.view.ViewParent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.util.containsKey
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView

/**
 * Manages the various graphs needed for a [NavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 *
 * Your navGraphIds must have the same ids as your menuItem ids
 */
fun NavigationView.setupWithNavController(
    fragmentManager: FragmentManager,
    parentNavController: NavController,
    navGraphIds: List<Int>,
    containerId: Int,
    currentItemId: Int,
    intent: Intent
): LiveData<NavController> {

    // Map of tags
    val graphIdToTagMap = SparseArray<String>()

    // Result. Mutable live data with the selected controller
    val selectedNavController = MutableLiveData<NavController>()

    // First create a NavHostFragment for each NavGraph ID
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )

        // Obtain its id
        val graphId = navHostFragment.navController.graph.id

        // Save to the map
        graphIdToTagMap[graphId] = fragmentTag

        // Attach or detach nav host fragment depending on whether it's the selected item
        if (graphId == currentItemId) {
            selectedNavController.value = navHostFragment.navController
            attachNavHostFragment(fragmentManager, navHostFragment)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }
    }

    // When a navigation item is selected
    setNavigationItemSelectedListener { item: MenuItem ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newItemId = item.itemId
            if (!graphIdToTagMap.containsKey(newItemId)) {
                // If the selected item is meant to be a destination separate
                // to the supplied graphs, navigate to it from the parent navController
                parentNavController.navigate(newItemId)
                return@setNavigationItemSelectedListener true
            }

            val newlySelectedItemTag = graphIdToTagMap[newItemId]
            val selectedFragment =
                fragmentManager.findFragmentByTag(newlySelectedItemTag) as NavHostFragment

            // Optional: When the already selected item is re-selected
            if (checkedItem!!.itemId == newItemId) {
                return@setNavigationItemSelectedListener popToStart(selectedFragment)
            }

            showSelectedFragment(
                fragmentManager,
                selectedNavController,
                selectedFragment,
                graphIdToTagMap
            )
            hideDrawer()
            true
        }
    }

    // Optional: handle deep links
    setupDeepLinks(
        fragmentManager,
        selectedNavController,
        graphIdToTagMap,
        navGraphIds,
        containerId,
        intent
    )

    return selectedNavController
}

private fun NavigationView.popToStart(selectedFragment: NavHostFragment): Boolean {
    val navController = selectedFragment.navController
    // Pop the back stack to the start destination of the current navController graph
    navController.popBackStack(
        navController.graph.startDestination, false
    )
    hideDrawer()
    return true
}

private fun NavigationView.hideDrawer() {
    val parent: ViewParent = parent
    if (parent is DrawerLayout) {
        parent.closeDrawer(this)
    } else {
        val bottomSheetBehavior = findBottomSheetBehavior(this)
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
}

private fun NavigationView.setupDeepLinks(
    fragmentManager: FragmentManager,
    selectedNavController: MutableLiveData<NavController>,
    graphIdToTagMap: SparseArray<String>,
    navGraphIds: List<Int>,
    containerId: Int,
    intent: Intent
) {
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )
        // Handle Intent
        val graphId = navHostFragment.navController.graph.id
        if (navHostFragment.navController.handleDeepLink(intent)
            && checkedItem!!.itemId != graphId
        ) {
            setCheckedItem(graphId)
            val selectedTag = graphIdToTagMap[graphId]
            showSelectedFragment(
                fragmentManager,
                selectedNavController,
                fragmentManager.findFragmentByTag(selectedTag) as NavHostFragment,
                graphIdToTagMap
            )
        }
    }
}

private fun showSelectedFragment(
    fragmentManager: FragmentManager,
    selectedNavController: MutableLiveData<NavController>,
    selectedFragment: NavHostFragment,
    graphIdToTagMap: SparseArray<String>
) {
    fragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.nav_default_enter_anim,
            R.anim.nav_default_exit_anim,
            R.anim.nav_default_pop_enter_anim,
            R.anim.nav_default_pop_exit_anim
        )
        .attach(selectedFragment)
        .setPrimaryNavigationFragment(selectedFragment)
        .apply {
            // Detach all other Fragments
            graphIdToTagMap.forEach { _, fragmentTag ->
                if (fragmentTag != selectedFragment.tag) {
                    detach(fragmentManager.findFragmentByTag(fragmentTag)!!)
                }
            }
        }
        .setReorderingAllowed(true)
        .commit()
    selectedNavController.value = selectedFragment.navController
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            setPrimaryNavigationFragment(navHostFragment)
        }
        .commitNow()
}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun getFragmentTag(index: Int) = "navigationView#$index"

/**
 * Copied from [NavigationUI.findBottomSheetBehavior] for visibility.
 *
 * Walks up the view hierarchy, trying to determine if the given View is contained within
 * a bottom sheet.
 */
private fun findBottomSheetBehavior(view: View): BottomSheetBehavior<*>? {
    val params = view.layoutParams
    if (params !is CoordinatorLayout.LayoutParams) {
        val parent = view.parent
        return if (parent is View) {
            findBottomSheetBehavior(parent as View)
        } else null
    }
    val behavior = params
        .behavior
    return if (behavior !is BottomSheetBehavior<*>) {
        // We hit a CoordinatorLayout, but the View doesn't have the BottomSheetBehavior
        null
    } else behavior
}
