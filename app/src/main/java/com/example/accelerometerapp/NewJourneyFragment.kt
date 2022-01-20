package com.example.accelerometerapp

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_new_journey.*
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewJourneyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewJourneyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
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

        return inflater.inflate(R.layout.fragment_new_journey, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonNext.setOnClickListener { view -> run {
            mainViewModel.journeyName = editTextName.text.toString()
            mainViewModel.journeyAdditionalComments = editTextAdditionalComments.text.toString()

            mainViewModel.routeID = Random.nextInt().toString()
            mainViewModel.myRef.child(mainViewModel.userId).child(mainViewModel.routeID).child("name").setValue(mainViewModel.journeyName)
            mainViewModel.myRef.child(mainViewModel.userId).child(mainViewModel.routeID).child("description").setValue(mainViewModel.journeyAdditionalComments)
            mainViewModel.myRef.child(mainViewModel.userId).child(mainViewModel.routeID).child("startRoute").setValue(System.currentTimeMillis().toString())

            view.findNavController().navigate(R.id.action_newJourneyFragment_to_recordingFragment)
        } }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                mainViewModel.auth.signOut()
                requireView().findNavController().navigate(R.id.action_newJourneyFragment_to_loginFragment)
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
         * @return A new instance of fragment NewJourneyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewJourneyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}