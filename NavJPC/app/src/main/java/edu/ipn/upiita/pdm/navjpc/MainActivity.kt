package edu.ipn.upiita.pdm.navjpc
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.material3.*

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import edu.ipn.upiita.pdm.navjpc.ui.Navigator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigator()
        }
    }
}
