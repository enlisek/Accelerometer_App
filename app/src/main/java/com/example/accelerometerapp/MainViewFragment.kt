package com.example.accelerometerapp

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_main_view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainViewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mainViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(
            Application()
        )).get(MainViewModel::class.java)

        setHasOptionsMenu(true)

        viewManager= LinearLayoutManager(requireContext())
        return inflater.inflate(R.layout.fragment_main_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = viewManager
        setupAdapter(mainViewModel.listOfJourneys)

        mainViewModel.userRef = mainViewModel.database.getReference("test6/"+mainViewModel.userId)
        mainViewModel.userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                mainViewModel.listOfJourneys = ArrayList()

                for (row in datasnapshot.children) {
                    val newRow = row.getValue(JourneyRow::class.java)
                    mainViewModel.listOfJourneys.add(newRow!!)
                }
                if (recyclerView != null)
                {
                    setupAdapter(mainViewModel.listOfJourneys)
                }
            }
        })

        NewJourneyButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_mainViewFragment_to_newJourneyFragment)
        }
    }

    private fun setupAdapter(arrayData : ArrayList<JourneyRow>){
        recyclerView.adapter = JourneyAdapter(arrayData)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                mainViewModel.auth.signOut()
                requireView().findNavController().navigate(R.id.action_mainViewFragment_to_loginFragment)
                return true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_app_bar, menu)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}