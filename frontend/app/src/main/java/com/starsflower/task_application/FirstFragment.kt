package com.starsflower.task_application

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.starsflower.task_application.databinding.FragmentTaskListBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val dataViewModel: MainDataViewModel by activityViewModels()
    private val taskDataViewModel: TaskDataViewModel by activityViewModels()
    private val client = OkHttpClient()

    private lateinit var tasksListView: ListView
    private lateinit var notAssignedTasksListView: ListView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayTaskList(view)

        binding.fabAddTask.setOnClickListener { view
            taskDataViewModel.empty()

            findNavController().navigate(R.id.action_MainScreen_to_AddTaskScreen)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Ignore unknown keys until due time data is implemented
    private val json = Json { ignoreUnknownKeys = true }

    private fun displayTaskList(view: View) {
        tasksListView = binding.tasksListView
        notAssignedTasksListView = binding.tasksNotAssignedListView

        // Load tasks
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "list")))
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
                var data = json.decodeFromString<Error>(response.toString());
                Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()
            } else {
                var data = json.decodeFromString<TaskList>(response.toString());
                var assignedTasks = ArrayList<Task>()
                var notAssignedTasks = ArrayList<Task>()

                data.tasks.forEach { task ->
                    if (task.assigned_users.contains(dataViewModel.user_id.value!!)) {
                        assignedTasks.add(task)
                    } else {
                        notAssignedTasks.add(task)
                    }
                }

                setDataAndListener(assignedTasks, tasksListView)
                setDataAndListener(notAssignedTasks, notAssignedTasksListView)
            }
        }
    }

    private fun setDataAndListener(tasks: ArrayList<Task>, listView: ListView) {
        val listItems = arrayOfNulls<String>(tasks.size)

        // Set data in list to task details
        tasks.forEachIndexed { idx, it ->
            listItems[idx] = it.content
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        listView.adapter = adapter

        // On click listener!
        listView.setOnItemClickListener { _, _, position, _ ->
            val element = tasks[position]

            // Populate page
            taskDataViewModel.setTaskID(element.task_id)
            taskDataViewModel.setContent(element.content)
            taskDataViewModel.setAuthorID(element.author_id ?: 0)
            taskDataViewModel.setAssignedUsers(element.assigned_users)

            findNavController().navigate(R.id.action_MainScreen_to_AddTaskScreen)
        }

        Utils.setListViewHeightBasedOnChildren(listView)
    }
}