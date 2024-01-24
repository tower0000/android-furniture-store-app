package com.example.koti.ui.view.fragments.auth

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.koti.R
import com.example.koti.databinding.FragmentLoginBinding
import com.example.koti.ui.util.RegisterValidation
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.changeButtonBackgroundDrawable
import com.example.koti.ui.util.changeEdBackgroundDrawable
import com.example.koti.ui.util.changeHintSizeWhenTextExists
import com.example.koti.ui.util.setupBottomSheetDialog
import com.example.koti.ui.view.activities.ShoppingActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.koti.ui.viewmodel.LoginViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.buttonGoogleLogin.setOnClickListener {
            signInGoogle()
        }

        changeHintSizeWhenTextExists(binding.edEmail)
        changeHintSizeWhenTextExists(binding.edPass)

        binding.buttonRedirectToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.apply {
            buttonLogin.setOnClickListener {
                val email = edEmail.text.toString().trim()
                val password = edPass.text.toString()
                viewModel.login(email, password)
            }
        }

        binding.buttonForgotPassword.setOnClickListener {
            setupBottomSheetDialog { email ->
                viewModel.resetPassword(email)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetPassword.collect {
                    when (it) {
                        is Resource.Loading -> {
                        }

                        is Resource.Success -> {
                            Snackbar.make(
                                requireView(),
                                getString(R.string.reset_link_sent),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(),
                                "Error ${it.message}",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.login.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonLogin.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonLogin.revertAnimation()
                            changeButtonBackgroundDrawable(
                                binding.buttonLogin,
                                R.drawable.green_button_background,
                                requireContext()
                            )
                            Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                            }
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            binding.buttonLogin.revertAnimation()
                            changeButtonBackgroundDrawable(
                                binding.buttonLogin,
                                R.drawable.green_button_background,
                                requireContext()
                            )
                        }

                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { validation ->
                    if (validation.email is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.edEmail.apply {
                                Toast.makeText(
                                    requireContext(),
                                    validation.email.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        changeEdBackgroundDrawable(
                            binding.textFieldEmail,
                            R.drawable.error_edit_text_background,
                            requireContext()
                        )
                    } else {
                        changeEdBackgroundDrawable(
                            binding.textFieldEmail,
                            R.drawable.white_edit_text_background,
                            requireContext()
                        )
                    }
                    if (validation.password is RegisterValidation.Failed) {
                        withContext(Dispatchers.Main) {
                            binding.edPass.apply {
                                Toast.makeText(
                                    requireContext(),
                                    validation.password.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        changeEdBackgroundDrawable(
                            binding.textFieldPass,
                            R.drawable.error_edit_text_background,
                            requireContext()
                        )
                    } else {
                        changeEdBackgroundDrawable(
                            binding.textFieldPass,
                            R.drawable.white_edit_text_background,
                            requireContext()
                        )
                    }
                }
            }
        }
    }

    private val googleSignInResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResult(task)
            } else {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }


    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        googleSignInResult.launch(signInIntent)
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val name = account.displayName.toString()
                val email = account.email.toString()
                val img = account.photoUrl.toString()
                viewModel.saveUserData(name, email, img)

                Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }
}
