package com.starsflower.task_application

import android.content.Context
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
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val dataViewModel: MainDataViewModel by activityViewModels()
    private val client = OkHttpClient()

    private lateinit var listView: ListView

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

        this.listView = binding.tasksListView

        // Load tasks
        val url = URL(dataViewModel.createURL(arrayOf("tasks", "list")))
        var request = Request.Builder()
            .header("X-Authenticate", dataViewModel.jwt.value!!)
            .url(url)
            .get()
            .build()

        this.client.newCall(request).execute().use { it
            val response = it.body!!.string()

            if (!it.isSuccessful) {
                // Show error
                var data = Json.decodeFromString<Error>(response.toString());
                Snackbar.make(view, data.error, Snackbar.LENGTH_SHORT).show()
            } else {
                // Ignore unknown keys until due data is implemented
                var data = Json{ignoreUnknownKeys=true}.decodeFromString<TaskList>(response.toString());

                val listItems = arrayOfNulls<String>(data.tasks.size)
                data.tasks.forEachIndexed { idx, it ->
                    listItems[idx] = it.content
//                    Snackbar.make(view, it.content, Snackbar.LENGTH_SHORT).show()
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
                listView.adapter = adapter

            }
        }

        binding.fabAddTask.setOnClickListener { view
            findNavController().navigate(R.id.action_MainScreen_to_AddTaskScreen)

            Snackbar.make(view, dataViewModel.jwt.value!!, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}