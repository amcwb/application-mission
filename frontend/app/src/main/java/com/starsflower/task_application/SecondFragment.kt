package com.starsflower.task_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.view.get
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

    private lateinit var users: Array<User>
    private lateinit var listView: ListView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserList()
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

    // Ignore unknown keys until due time data is implemented
    private val json = Json

    private fun getUserList() {
        listView = binding.usersListView
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE;

        val url = URL(dataViewModel.createURL(arrayOf("users", "list")))
        var request = Request.Builder()
            .header("X-Authenticate", dataViewModel.jwt.value!!)
            .url(url)
            .get()
            .build()

        // Fetch tasks
        this.client.newCall(request).execute().use { it
            val response = it.body!!.string()

            if (!it.isSuccessful) {
                // Show error
                var data = json.decodeFromString<Error>(response);

                Snackbar.make(requireView(), "Unable to load user list for assigning", Snackbar.LENGTH_LONG)
                    .show()
            } else {
                var data = json.decodeFromString<UserList>(response);

                val listItems = arrayOfNulls<String>(data.users.size)

                // Set data in list to task details
                users = data.users

                data.users.forEachIndexed { idx, it ->
                    listItems[idx] = it.name + " " + it.surname
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, listItems)
                listView.adapter = adapter

                data.users.forEachIndexed { idx, it ->
                    // Is user in assigned users?
                    var isThisUserSelected = taskDataViewModel.assigned_users.value?.contains(it.user_id) == true

                    listView.setItemChecked(idx, isThisUserSelected)
                }

                Utils.setListViewHeightBasedOnChildren(listView)
            }
        }
    }

    private fun setDataFromTaskView() {
        // Set task content
        binding.editTaskContent.setText(taskDataViewModel.content.value ?: "Task Content")
    }

    private fun buildFormBody(): FormBody {
        // Get input
        val content = binding.editTaskContent.text.toString()

        // Get user IDs selected, separated by comma
        var assignedUserIds = ArrayList<Int>()
        users.forEachIndexed { index, user ->
            if (listView.isItemChecked(index)) {
                assignedUserIds.add(user.user_id)
            }
        }

        var formBody = FormBody.Builder()
            .add("content", content)
            .add(
                "assigned_users",
                assignedUserIds.joinToString(",")
            )


        // Patch if task ID
        var taskID = taskDataViewModel.task_id.value
        if (taskID != null) {
            formBody.add("task_id", taskID!!.toString())
        }

        return formBody.build()
    }

    private fun buildRequest(url: URL): Request {
        return Request.Builder()
            .url(url)
            .header("X-Authenticate", dataViewModel.jwt.value!!)
            .post(buildFormBody())
            .build()
    }

    private fun createNewTaskRequest(view: View): Boolean {
        // Create URL
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "create")))

        // Try request
        var request = buildRequest(url)

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
        // Create URL
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "set_data")))

        // Try request
        var request = buildRequest(url)

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