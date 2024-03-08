package com.codingwithsaurav.firebasegooglesignin

import android.Manifest.permission_group.CALENDAR
import android.content.Intent
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
import com.codingwithsaurav.firebasegooglesignin.updateeData.CategoryFragment
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var showOneTapUI = true
    private var oneTapClient: SignInClient? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        startActivity(Intent(this, CategoryActivity::class.java))
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


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(CALENDAR))
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding?.singInWithGoogle?.setOnClickListener {
            auth.signOut()
            mGoogleSignInClient?.signOut()
            mGoogleSignInClient?.signInIntent?.let {
                Log.w("klnflkadsnflaks", "startActivityForResult")
                startActivityForResult(it, 112)
            }
            /*if (showOneTapUI)
                lifecycleScope.launch(Dispatchers.Main) {
                    launchSignInIntent()
                }*/
        }
        auth.signOut()
        updateUI(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 112) {
            Log.w("klnflkadsnflaks", "resultCode $requestCode")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {

            val account = completedTask.getResult(ApiException::class.java)
            val authCode = account.serverAuthCode
            Log.w("klnflkadsnflaks", "authCode=" + authCode)
            getRefreshToken(authCode)
            binding?.apply {
                nameTextView.text = account.displayName.toString()
                emailTextView.text = account.email.toString()
            }
            binding?.profileImageView?.let { image ->
                Glide.with(this)
                    .load(account.photoUrl.toString())
                    .into(image)
            }
            // Signed in successfully, show authenticated UI.
            //updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("klnflkadsnflaks", "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun getRefreshToken(authCode: String?) {
        Log.w("klnflkadsnflaks", "authCode=" + authCode.toString())
        val retrofit = Retrofit.Builder()
            .baseUrl("https://oauth2.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(OAuthService::class.java)
        val call = service.getToken(
            code = authCode.toString(),
            clientId = getString(R.string.default_web_client_id),
            clientSecret = "c0a33b096789bd9111b91c05695210c341cca48b",
            redirectUri = "",
            grantType = "authorization_code"
        )
        call.enqueue(object : Callback<GoogleTokenResponse> {
            override fun onResponse(
                call: Call<GoogleTokenResponse>,
                response: Response<GoogleTokenResponse>
            ) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    Log.w("klnflkadsnflaks", "tokenResponse=" + tokenResponse.toString())
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<GoogleTokenResponse>, t: Throwable) {
                // Handle failure
            }
        })
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