package com.example.koti.ui.view.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.koti.ui.viewmodel.RegisterViewModel
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
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success), Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Error -> {
                            binding.buttonRegister.revertAnimation()
                            binding.buttonRegister.setBackgroundDrawable(resources.getDrawable(R.drawable.green_button_background))
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
                        setEmailFieldOnError()
                    } else {
                        setEmailFieldOnNormal()
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
                        setPassFieldOnError()
                    } else {
                        setPassFieldOnNormal()
                    }
                }
            }
        }
    }

    private fun setEmailFieldOnError(){
        binding.textFieldEmail.setBackgroundDrawable(resources.getDrawable(R.drawable.error_edit_text_background))
    }

    private fun setEmailFieldOnNormal(){
        binding.textFieldEmail.setBackgroundDrawable(resources.getDrawable(R.drawable.white_edit_text_background))
    }

    private fun setPassFieldOnError(){
        binding.textFieldPass.setBackgroundDrawable(resources.getDrawable(R.drawable.error_edit_text_background))
    }

    private fun setPassFieldOnNormal(){
        binding.textFieldPass.setBackgroundDrawable(resources.getDrawable(R.drawable.white_edit_text_background))
    }
}