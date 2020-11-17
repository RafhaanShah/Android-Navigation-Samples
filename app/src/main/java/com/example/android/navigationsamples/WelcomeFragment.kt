package com.example.android.navigationsamples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        val buttonBottomNav = view.findViewById<Button>(R.id.welcome_button_bottom_nav)
        buttonBottomNav.setOnClickListener {
            navController.navigate(R.id.bottom_nav)
        }

        val drawerButton = view.findViewById<Button>(R.id.welcome_button_drawer)
        drawerButton.setOnClickListener {
            navController.navigate(R.id.drawer)
        }

        val viewPagerButton = view.findViewById<Button>(R.id.welcome_button_pager)
        viewPagerButton.setOnClickListener {
            navController.navigate(R.id.view_pager)
        }
    }

}
