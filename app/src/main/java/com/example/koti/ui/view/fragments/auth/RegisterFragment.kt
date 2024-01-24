package com.example.koti.ui.view.fragments.auth

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.koti.R
import com.example.koti.databinding.FragmentRegisterBinding
import com.example.koti.model.User
import com.example.koti.ui.util.RegisterValidation
import com.example.koti.ui.util.Resource
import com.example.koti.ui.util.changeButtonBackgroundDrawable
import com.example.koti.ui.util.changeEdBackgroundDrawable
import com.example.koti.ui.util.changeHintSizeWhenTextExists
import com.example.koti.ui.viewmodel.RegisterViewModel
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changeHintSizeWhenTextExists(binding.edRegisterFirstName)
        changeHintSizeWhenTextExists(binding.edRegisterLastName)
        changeHintSizeWhenTextExists(binding.edRegisterEmail)
        changeHintSizeWhenTextExists(binding.edRegisterPass)

        binding.buttonRedirectToAuth.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.apply {
            buttonRegister.setOnClickListener {
                val user = User(
                    edRegisterFirstName.text.toString().trim(),
                    edRegisterLastName.text.toString().trim(),
                    edRegisterEmail.text.toString().trim(),
                    ""
                )
                val password = edRegisterPass.text.toString()
                viewModel.createAccountWithEmailAndPassword(user, password)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonRegister.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonRegister.revertAnimation()
                            changeButtonBackgroundDrawable(
                                binding.buttonRegister,
                                R.drawable.green_button_background,
                                requireContext()
                            )
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success), Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Error -> {
                            binding.buttonRegister.revertAnimation()
                            changeButtonBackgroundDrawable(
                                binding.buttonRegister,
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
                            binding.edRegisterEmail.apply {
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
                            binding.edRegisterPass.apply {
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
}