package com.starsflower.task_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.starsflower.task_application.databinding.FragmentAddTaskBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val dataViewModel: MainDataViewModel by activityViewModels()
    private val taskDataViewModel: TaskDataViewModel by activityViewModels()
    private val client = OkHttpClient()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDataFromTaskView()
        binding.saveTask.setOnClickListener {
            // Is this a new task or a saved task?

            var success: Boolean
            if (this.taskDataViewModel.task_id.value == null) {
                // This is a new task!
                success = createNewTaskRequest(view)
            } else {
                success = updateCurrentTask(view)
            }

            if (success) {
                // Empty data
                this.taskDataViewModel.empty()

                // Navigate back to main!
                findNavController().navigate(R.id.action_AddTaskScreen_to_MainScreen)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDataFromTaskView() {
        // Set task content
        binding.editTaskContent.setText(taskDataViewModel.content.value ?: "Task Content")
    }

    private fun createNewTaskRequest(view: View): Boolean {
        // Get input
        val content = binding.editTaskContent.text.toString()

        // Create URL
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "create")))

        // Try login
        var formBody = FormBody.Builder()
            .add("content", content)
            .build()

        var request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        this.client.newCall(request).execute().use { it
            val response = it.body!!.string()

            if (!it.isSuccessful) {
                // Show error
                var data = Json.decodeFromString<Error>(response);
                Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()

                return false
            } else {
                // Consume response, may be useful later?
                Json.decodeFromString<TaskID>(response);
                Snackbar.make(view, "Created task successfully", Snackbar.LENGTH_SHORT).show()

                return true
            }
        }

    }

    private fun updateCurrentTask(view: View): Boolean {
        // Get input
        var task_id = taskDataViewModel.task_id.value!!
        val content = binding.editTaskContent.text.toString()

        // Create URL
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "set_data")))

        // Try login
        var formBody = FormBody.Builder()
            .add("task_id", task_id.toString())
            .add("content", content)
            .build()

        var request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        this.client.newCall(request).execute().use { it
            val response = it.body!!.string()

            if (!it.isSuccessful) {
                // Show error
                var data = Json.decodeFromString<Error>(response);
                Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()

                return false
            } else {
                // Updating returns no data
                Snackbar.make(view, "Task updated successfully", Snackbar.LENGTH_SHORT).show()

                return true
            }
        }
    }
}