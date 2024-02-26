package com.codingwithsaurav.firebasegooglesignin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.codingwithsaurav.firebasegooglesignin.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var showOneTapUI = true
    private var oneTapClient: SignInClient? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        binding?.singInWithGoogle?.setOnClickListener {
            auth.signOut()
            if (showOneTapUI)
                lifecycleScope.launch(Dispatchers.Main) {
                    launchSignInIntent()
                }
        }
        auth.signOut()
        updateUI(null)
    }

    private suspend fun launchSignInIntent() {
        showOneTapUI = false
        oneTapClient?.beginSignIn(signInRequest)?.await()?.let { result ->
            val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent).build()
            activityResultLauncher.launch(intentSenderRequest)
        }
    }

    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            showOneTapUI = true
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient?.getSignInCredentialFromIntent(result.data)
                    val idToken = credential?.googleIdToken
                    when {
                        idToken != null -> {
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("sdfsdfsf", "signInWithCredential:success")
                                        val user = auth.currentUser
                                        updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(
                                            "sdfsdfsf",
                                            "signInWithCredential:failure",
                                            task.exception
                                        )
                                        updateUI(null)
                                    }
                                }
                            Log.d("sdfsdfsf", "Got ID token.")
                        }

                        else -> {
                            // Shouldn't happen.
                            Log.d("sdfsdfsf", "No ID token!")
                        }
                    }
                } catch (e: ApiException) {

                }
            }
        }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            user?.let { it ->
                binding?.apply {
                    nameTextView.text = it.displayName
                    emailTextView.text = it.email
                }
                binding?.profileImageView?.let { image ->
                    Glide.with(this)
                        .load(it.photoUrl)
                        .into(image)
                }
            }
        } else {
            val it = Firebase.auth.currentUser
            binding?.apply {
                nameTextView.text = it?.displayName
                emailTextView.text = it?.email
            }
            binding?.profileImageView?.let { image ->
                Glide.with(this)
                    .load(it?.photoUrl)
                    .into(image)
            }
        }
    }

}