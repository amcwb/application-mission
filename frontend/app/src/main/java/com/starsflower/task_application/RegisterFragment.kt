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
import com.starsflower.task_application.databinding.FragmentRegisterBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val dataViewModel: MainDataViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions(
            Array<String>(1) { Manifest.permission.INTERNET },
            1
        )

        // LOGIN!
        binding.registerButton.setOnClickListener {
            // Get input
            val name = binding.registerNameInput.text.toString()
            val surname = binding.registerSurnameInput.text.toString()
            val email = binding.registerEmailAddressInput.text.toString()
            val password = binding.registerPasswordInput.text.toString()
            val repeatPassword = binding.registerRepeatPasswordInput.text.toString()

            // Create URL
            val url = URL(dataViewModel.createURL(arrayOf("users", "create")))

            // Try login
            var formBody = FormBody.Builder()
                .add("name", name)
                .add("surname", surname)
                .add("email", email)
                .add("password", password)
                .add("confirm_password", repeatPassword)
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
                    Snackbar.make(view, "Created user successfully", Snackbar.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_registerFragment_to_FirstFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}