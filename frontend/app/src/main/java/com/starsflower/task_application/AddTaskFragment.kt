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
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val dataViewModel: MainDataViewModel by activityViewModels()
    private val taskDataViewModel: TaskDataViewModel by activityViewModels()

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

        // Get list of users and check users assigned
        getUserList()

        // Set data not pertaining to users in the view
        setNonUserDataFromView()

        binding.saveTask.setOnClickListener {
            onSavePress(view)
        }

        binding.deleteTask.setOnClickListener {
            onDeletePress(view)
        }
    }

    private fun onSavePress(view: View) {
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
            findNavController().navigateUp()
        }
    }

    private fun onDeletePress(view: View) {
        if (taskDataViewModel.task_id.value == null) {
            this.taskDataViewModel.empty()
            findNavController().navigateUp()
            return
        }

        // Process deletion
        val formBody = FormBody.Builder()
            .add("task_id", taskDataViewModel.task_id.value?.toString()!!)
            .build()

        val url = URL(dataViewModel.createURL(arrayOf("tasks", "delete")))
        val request = buildAuthedRequest(url, formBody)

        Utils.makeSafeRequest(request, requireView()) {
            if (!it.isSuccessful) {
                Snackbar.make(view, "Unable to delete!", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                Snackbar.make(view, "Deleted task", Snackbar.LENGTH_SHORT)
                    .show()

                // Empty data
                this.taskDataViewModel.empty()
                findNavController().navigateUp()
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
        Utils.makeSafeRequest(request, requireView()) {
            if (!it.isSuccessful) {
                // Error details are not provided specifically.
                // json.decodeFromString<Error>(response);

                Snackbar.make(requireView(), "Unable to load user list for assigning", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                var data = json.decodeFromString<UserList>(it.body!!.string());

                val listItems = arrayOfNulls<String>(data.users.size)

                // Set data in list to task details
                users = data.users

                data.users.forEachIndexed { idx, it ->
                    listItems[idx] = it.name + " " + it.surname
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_multiple_choice,
                    listItems
                )
                listView.adapter = adapter

                data.users.forEachIndexed { idx, it ->
                    // Is user in assigned users?
                    var isThisUserSelected =
                        taskDataViewModel.assigned_users.value?.contains(it.user_id) == true

                    // Set checked items here
                    listView.setItemChecked(idx, isThisUserSelected)
                }
            }
        }
    }

    private fun setNonUserDataFromView() {
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

        // Create body with information
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

    private fun buildAuthedRequest(url: URL, body: FormBody): Request {
        return Request.Builder()
            .url(url)
            .header("X-Authenticate", dataViewModel.jwt.value!!)
            .post(body)
            .build()
    }

    private fun buildRequest(url: URL): Request {
        return buildAuthedRequest(url, buildFormBody())
    }

    private fun createNewTaskRequest(view: View): Boolean {
        // Create URL
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "create")))

        // Try request
        var request = buildRequest(url)

        return Utils.makeSafeRequest(request, requireView()) {
            if (!it.isSuccessful) {
                // Show error
                var data = Json.decodeFromString<Error>(it.body!!.string());
                Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()

                return@makeSafeRequest false
            } else {
                // Consume response, may be useful later?
                Json.decodeFromString<TaskID>(it.body!!.string());
                Snackbar.make(view, "Created task successfully", Snackbar.LENGTH_SHORT).show()

                return@makeSafeRequest true
            }
        }
    }

    private fun updateCurrentTask(view: View): Boolean {
        // Create URL
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "set_data")))

        // Try request
        var request = buildRequest(url)

        return Utils.makeSafeRequest(request, requireView()) {
            if (!it.isSuccessful) {
                // Show error
                var data = Json.decodeFromString<Error>(it.body!!.string());
                Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()

                return@makeSafeRequest false
            } else {
                // Updating returns no data
                Snackbar.make(view, "Task updated successfully", Snackbar.LENGTH_SHORT).show()

                return@makeSafeRequest true
            }
        }
    }
}