package com.example.koti.ui.util

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.koti.R
import com.example.koti.model.SignInResult
import com.example.koti.model.User
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthUiClient (
    private val context: Context,
) {

}