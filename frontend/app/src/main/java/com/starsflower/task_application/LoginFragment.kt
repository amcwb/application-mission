package com.starsflower.task_application

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.starsflower.task_application.databinding.FragmentLoginBinding
import java.net.URL
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val dataViewModel: MainDataViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions(
            Array<String>(1) { Manifest.permission.INTERNET },
            1
        )

        // LOGIN!
        binding.loginButton.setOnClickListener {
            // Get input
            val email = binding.emailAddressInput.text.toString()
            val password = binding.passwordInput.text.toString()

            // Create URL
            val url = URL(dataViewModel.createURL(arrayOf("users", "login")))

            // Try login
            var formBody = FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build()

            var request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()


            Utils.makeSafeRequest(request, view) {
                if (!it.isSuccessful) {
                    // Show error
                    var data = Json.decodeFromString<Error>(it.body!!.string());
                    Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()
                } else {
                    var data = Json.decodeFromString<JWTResponse>(it.body!!.string());
                    dataViewModel.setJWT(data.jwt)
                    dataViewModel.setUserID(data.user_id)
                    Snackbar.make(view, "Logged in successfully", Snackbar.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_LoginFragment_to_ListFragment)
                }
            }
        }

        // Register
        binding.goToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}