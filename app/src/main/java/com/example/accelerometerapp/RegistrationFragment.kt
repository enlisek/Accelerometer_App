package com.example.accelerometerapp

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_registration.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrationFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonRegister.setOnClickListener { view -> run{

            var email = editTextTextEmailAddress.text.toString().trim()
            var pass1 = editTextTextPassword1.text.toString()

            if (email.isEmpty())
            {
                editTextTextEmailAddress.error = "E-mail is required."
            }
            else
            {
                if (pass1 != editTextTextPassword2.text.toString())
                {
                    editTextTextPassword1.error = "Those passwords didn’t match."
                }
                else
                {
                    if(pass1.length < 6)
                    {
                        editTextTextPassword1.error = "Password should have at least 6 characters."
                    }
                    else
                    {
                        mainViewModel.auth.createUserWithEmailAndPassword(email,pass1).addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                Toast.makeText(context,"User created.", Toast.LENGTH_SHORT).show()
                                view.findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)

                            } else {
                                Toast.makeText(context,"Error. Try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
        }
        buttonRegisterToLogin.setOnClickListener { view -> view.findNavController().navigate(R.id.action_registrationFragment_to_loginFragment) }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegistrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}