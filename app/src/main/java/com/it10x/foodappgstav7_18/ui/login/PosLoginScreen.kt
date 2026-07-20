package com.it10x.foodappgstav7_18.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_18.auth.PosUser

@Composable
fun PosLoginScreen(
    users: List<PosUser>,
    isLoading: Boolean,
    error: String?,
    onLoginClick: (PosUser, String) -> Unit
) {

    var selectedUser by remember {
        mutableStateOf<PosUser?>(null)
    }

    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "POS Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Select User",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {

            if (isLoading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {

                LazyColumn {

                    items(users) { user ->

                        val selected =
                            selectedUser?.id == user.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                                .clickable {

                                    selectedUser = user

                                },
                            border =
                                if (selected)
                                    BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    )
                                else
                                    null
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = user.role
                                )

                            }

                        }

                    }

                }

            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            enabled = selectedUser != null,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        if (!error.isNullOrEmpty()) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled =
                selectedUser != null &&
                        password.isNotBlank() &&
                        !isLoading,
            onClick = {

                onLoginClick(
                    selectedUser!!,
                    password
                )

            }
        ) {

            Text("LOGIN")

        }

    }

}