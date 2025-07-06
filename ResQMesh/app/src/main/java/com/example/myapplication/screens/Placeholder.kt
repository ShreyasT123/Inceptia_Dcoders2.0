package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Placeholder(navController: NavController) {
    val userName = "John Doe"
    val phoneNumber = "+91 9876543210"

    val helplines = listOf(
        "Police" to "100",
        "Ambulance" to "102",
        "Fire Brigade" to "101",
        "Disaster Management" to "108",
        "Women's Helpline" to "1091"
    )

    val redColor = Color(0xFFD32F2F)
    val cardBlack = Color(0xFF1A1A1A)
    val backgroundBlack = Color(0xFF000000)

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
        containerColor = backgroundBlack,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = redColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 👤 User Info Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = redColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Name: $userName", fontSize = 18.sp, color = Color.White)
                    Text("Phone: $phoneNumber", fontSize = 16.sp, color = Color.White)
                }
            }

            // 📞 Section Header
            Text(
                "Important Helpline Numbers",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = redColor
            )

            // 📇 Helpline Cards
            helplines.forEach { (title, number) ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBlack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontSize = 16.sp, color = Color.White)
                        Text(number, fontSize = 16.sp, color = redColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
