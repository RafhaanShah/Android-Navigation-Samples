# Android Navigation Samples

- This is a sample app showing the usage of the Android [Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started) in a Single-Activity-App, with nested Navigation Graphs with multiple back-stacks. 
- This sample is based on the [NavigationAdvancedSample](https://github.com/googlesamples/android-architecture-components/tree/master/NavigationAdvancedSample) by Google which was initially created as a workaround to handle multiple back stacks with Navigation and a [BottomNavigationView](https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView).
- This has been slightly modified, and the same principles have been applied to get similar functionality when using a [Navigation Drawer](https://developer.android.com/guide/navigation/navigation-ui) as well.
Lastly, a sample using a [View Pager](https://developer.android.com/guide/navigation/navigation-swipe-view-2) is also included which is similar to the other two.

<table>
  <tr>
    <td><img src='assets/demo_bottom_nav.gif'></td>
    <td><img src='assets/demo_drawer.gif'></td>
    <td><img src='assets/demo_paging.gif'></td>
  </tr>
</table>

## TL;DR Quickstart
- For Bottom Navigation: copy [BottomNavExtensions](app/src/main/java/com/example/android/navigationsamples/BottomNavExtensions.kt) into your project and set up your host Fragment based on this [BottomNavFragment](app/src/main/java/com/example/android/navigationsamples/BottomNavFragment.kt) with this [layout](app/src/main/res/layout/fragment_bottom_nav.xml)
- For a Navigation Drawer: copy [DrawerExtensions](app/src/main/java/com/example/android/navigationsamples/DrawerExtensions.kt) into your project and set up your host Fragment based on this [DrawerFragment](app/src/main/java/com/example/android/navigationsamples/DrawerFragment.kt) with this [layout](app/src/main/res/layout/fragment_drawer.xml)
- For a View Pager: follow this [ViewPagerFragment](app/src/main/java/com/example/android/navigationsamples/ViewPagerFragment.kt) and this [layout](app/src/main/res/layout/fragment_view_pager.xml)

## Changes
This sample differs from the original sample in a few ways:
- Follows a true Single-Activity-App architecture in which the Fragments do not depend on the implementation of the Activity in any way, and the Activity has no Views or UI except a [FragmentContainerView](https://developer.android.com/reference/androidx/fragment/app/FragmentContainerView)
- Allows changing the default starting tab for the Bottom Navigation View
- Allows changing the behaviour of the back button in the Bottom Navigation View, either always going to the 'start' fragment before exiting the host Fragment, or exiting the host directly

Additionally, it also adds the following:
- Extensions for the [NavigationView](https://developer.android.com/reference/com/google/android/material/navigation/NavigationView) that works with the Drawer Layout. This is based on the original examples [NavigationExtensions](https://github.com/android/architecture-components-samples/blob/master/NavigationAdvancedSample/app/src/main/java/com/example/android/navigationadvancedsample/NavigationExtensions.kt). This allows Fragment state and each graph's back-stack state to be maintained when changing between destinations.
- Example Fragments showing the usage of the Drawer Layout and View Pager.

## Usage Details
### Activity
Is only a container for the Fragments and has no other views so that your Fragment's are not tied to the Activity implementation in any way and you don't need to interact with it to change any views or anything else
```
class MainActivity : AppCompatActivity() {  
    override fun onCreate(savedInstanceState: Bundle?) {  
        super.onCreate(savedInstanceState)  
        setContentView(R.layout.activity_main)  
    }  
}
```
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.fragment.app.FragmentContainerView xmlns:android="http://schemas.android.com/apk/res/android"  
  xmlns:app="http://schemas.android.com/apk/res-auto"  
  android:id="@+id/main_nav_container"  
  android:name="androidx.navigation.fragment.NavHostFragment"  
  android:layout_width="match_parent"  
  android:layout_height="match_parent"  
  app:defaultNavHost="true"  
  app:navGraph="@navigation/main" />
```
Note that `app:defaultNavHost="true"` is needed to properly handle the back-button presses with the Navigation Components. `app:navGraph="@navigation/main"` is for your top level navigation graph.

### Navigation Graphs
One graph is needed for your top level destinations, this would normally include your 'host' Fragment for your BottomNavigationView / DrawerLayout / ViewPager.
```
<navigation xmlns:android="http://schemas.android.com/apk/res/android"  
  xmlns:app="http://schemas.android.com/apk/res-auto"  
  xmlns:tools="http://schemas.android.com/tools"  
  android:id="@+id/main"  
  app:startDestination="@id/welcome">  
  
	<fragment  
	  android:id="@+id/welcome"  
	  android:name="com.example.android.navigationsamples.WelcomeFragment"  
	  android:label="Welcome"  
	  tools:layout="@layout/fragment_welcome" />  
  
	<fragment  android:id="@+id/bottom_nav"  
	  android:name="com.example.android.navigationsamples.BottomNavFragment"  
	  android:label="Bottom Nav"  
	  tools:layout="@layout/fragment_bottom_nav" />
</navigation>
```
- Each one of your other 'tabs' would have their own navigation graphs. 
- It is also very important to match your `id` fields from your graphs to the `id` fields of the `items` in your `menus` for the BottomNavigationView and NavigationView for the DrawerLayout, as these ID's are used to determine which graphs to show based on the pressed `menuItem`.

### Deep Links
If you have any [Deep Links](https://developer.android.com/guide/navigation/navigation-deep-link) in a inner nested tab Fragment, then you must have the same definition inside the top level graph for your host fragment as well:
```
main_nav_graph.xml
<fragment  
  android:id="@+id/bottom_nav"  
  android:name="com.example.android.navigationsamples.BottomNavFragment"  
  android:label="Bottom Nav"  
  tools:layout="@layout/fragment_bottom_nav">  
  <!-- Deep link to be handled inside sub-graph -->
  <deepLink  
    android:id="@+id/deepLink"  
    android:autoVerify="true"  
    app:uri="www.example.com/user/{userName}" />  
  <argument  android:name="userName"  
    app:argType="string" />  
</fragment>
```
```
tabA_nav_graph.xml
<fragment  
  android:id="@+id/userProfile"  
  android:name="com.example.android.navigationsamples.listscreen.UserProfile"  
  android:label="@string/title_detail"  
  tools:layout="@layout/fragment_user_profile">  
  <deepLink  android:id="@+id/deepLink"  
    app:uri="www.example.com/user/{userName}"  
    android:autoVerify="true"/>  
  <argument  android:name="userName"  
    app:argType="string"/>  
</fragment>
```

### Bottom Navigation Fragment
Your BottomNavigationView should be setup after the view has been created, the currently selected `menuItem` ID should also be saved in a variable so the correct destination is show on rotation and when you navigate away from the host and return, see [BottomNavFragment](app/src/main/java/com/example/android/navigationsamples/BottomNavFragment.kt) for the full implementation.
```
private var bottomNavSelectedItemId = R.id.home // Must be your starting destination
private fun setupBottomNavBar(view: View) {  
    val bottomNavView = view.findViewById<BottomNavigationView>(R.id.bottom_nav_view)  
    val toolbar = view.findViewById<Toolbar>(R.id.bottom_nav_toolbar)
	val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)  
  
    addToolbarListener(toolbar)  
    bottomNavView.selectedItemId = bottomNavSelectedItemId  
  
    val controller = bottomNavView.setupWithNavController(  
        fragmentManager = childFragmentManager,  
        navGraphIds = navGraphIds,  
        backButtonBehaviour = POP_HOST_FRAGMENT,  
        containerId = R.id.bottom_nav_container,  
        firstItemId = R.id.home, // Must be the same as bottomNavSelectedItemId  
	    intent = requireActivity().intent  
	)  
  
    controller.observe(viewLifecycleOwner, { navController ->  
	    NavigationUI.setupWithNavController(toolbar, navController)  
        bottomNavSelectedItemId = navController.graph.id  
  })  
}
```

### Drawer Fragment
The NavigationView should also be setup after your view has been created and the currently selected `menuItem` ID should also be saved in a variable, see [DrawerFragment](app/src/main/java/com/example/android/navigationsamples/DrawerFragment.kt). There is an extra parameter in the setup here for the `parentNavController`. This is needed if you have any destinations you wish to navigate to that are on the top level navigation graph, which would navigate away from this Drawer Fragment.
```
private var drawerSelectedItemId = R.id.home // Must be your starting destination,  
// same as the 'checked' one in your menu
private fun setupDrawer(view: View) {  
    drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout)  
    val toolbar = view.findViewById<Toolbar>(R.id.drawer_toolbar)  
    val navView = view.findViewById<NavigationView>(R.id.drawer_nav_view) 
    val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)  
  
    val controller = navView.setupWithNavController(  
        navGraphIds = navGraphIds,  
        fragmentManager = childFragmentManager,  
        containerId = R.id.drawer_container,  
        currentItemId = drawerSelectedItemId,  
        parentNavController = findNavController(),
        intent = requireActivity().intent  
  )  
  
    controller.observe(viewLifecycleOwner, { navController ->  
	    NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)  
        drawerSelectedItemId = navController.graph.id  
  })  
}
```
### View Pager
The adapter does the work in creating the Fragments for the ViewPager, we also need to `setPrimaryNavigationFragment` on the `FragmentManager` when the current tab changes, please see this [StackOverflow Post](https://stackoverflow.com/a/62629996) for more information. Also note that Deep Links do not work with the View Pager in this example. See [ViewPagerFragment](app/src/main/java/com/example/android/navigationsamples/ViewPagerFragment.kt).
```
private fun setupViewPager(view: View) {  
    val labels = listOf("Home", "Leaderboard", "Register")  
    val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)  
  
    val navHostFragmentAdapter = NavHostFragmentAdapter(  
        fragmentManager = childFragmentManager,  
        lifecycle = viewLifecycleOwner.lifecycle,  
        navGraphIds = navGraphIds  
    )  
    viewPager = view.findViewById(R.id.view_pager)  
    viewPager.adapter = navHostFragmentAdapter  
  
    val tabLayout = view.findViewById<TabLayout>(R.id.view_pager_tab_layout)  
    tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->  
	  tab.text = (labels[position])  
    }  
    tabLayoutMediator.attach() 
}
```
```
class NavHostFragmentAdapter(  
    fragmentManager: FragmentManager,  
    lifecycle: Lifecycle,  
    private val navGraphIds: List<Int>  
) : FragmentStateAdapter(fragmentManager, lifecycle) {  
  
    init {  
	  registerFragmentTransactionCallback(object : FragmentTransactionCallback() {  
            override fun onFragmentMaxLifecyclePreUpdated(  
                fragment: Fragment,  
                maxLifecycleState: Lifecycle.State  
		    ) = if (maxLifecycleState == Lifecycle.State.RESUMED) {  
              OnPostEventListener {  
			    fragment.parentFragmentManager.commitNow {  
			    setPrimaryNavigationFragment(fragment)  // So back button is handled properly
              }}} else { super.onFragmentMaxLifecyclePreUpdated(fragment, maxLifecycleState)}})  
	  }  
  
    override fun createFragment(position: Int): Fragment {  
        // Return a NEW fragment instance in createFragment(int)  
	    return NavHostFragment.create(navGraphIds[position])  
    }  
    override fun getItemCount(): Int = navGraphIds.size  
}
```


## License
[Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)

#### Also for the the original sample:
```
Copyright 2020 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. 
See the NOTICE file distributed with this work for additional information regarding copyright ownership. 
The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed 
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License 
for the specific language governing permissions and limitations under the License.
```
