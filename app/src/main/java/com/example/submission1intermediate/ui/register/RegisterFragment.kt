package com.example.submission1intermediate.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.submission1intermediate.R
import com.example.submission1intermediate.databinding.RegisterFragmentBinding
import com.example.submission1intermediate.utils.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class RegisterFragment : Fragment(), KodeinAware {

    override val kodein: Kodein by kodein()
    private val viewModelFactory: ViewModelFactory by instance()
    private lateinit var registerViewModel: RegisterViewModel
    private var _binding : RegisterFragmentBinding? = null
    private val binding get() = _binding!!
    private var registerJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        registerViewModel = ViewModelProvider(this, viewModelFactory)[RegisterViewModel::class.java]
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener{registerUser()}
    }


    private fun registerUser(){
        val name = binding.tiUsernameRegister.text.toString().trim()
        val email = binding.tiEmailRegister.text.toString().trim()
        val password = binding.tiPasswordRegister.text.toString()

        lifecycleScope.launchWhenResumed {
            if (registerJob.isActive) registerJob.cancel()

            registerJob = launch {
                registerViewModel.userRegister(name, email, password).collect{ result ->
                    result.onSuccess {
                        Toast.makeText(requireContext(), "Registration success", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    result.onFailure {
                        Snackbar.make(binding.root, "Registration failed", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}