package com.CodeNaroNa.vendor.relief.Fragments.HomeFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.CodeNaroNa.vendor.relief.Fragments.VendorDataFragment.VendorDataFragment
import com.CodeNaroNa.vendor.relief.GlobalHelpers.Resource
import com.CodeNaroNa.vendor.relief.GlobalHelpers.Utility
import com.CodeNaroNa.vendor.relief.R
import com.CodeNaroNa.vendor.relief.Repositories.CentralRepository
import com.CodeNaroNa.vendor.relief.Fragments.HomeFragment.ViewModels.HomeFragmentViewModel
import com.CodeNaroNa.vendor.relief.Fragments.HomeFragment.ViewModels.HomeFragmentViewModelFactory
import com.CodeNaroNa.vendor.relief.MainActivity.ViewModels.MainActivityViewModel
import com.CodeNaroNa.vendor.relief.SignUpActivity.SignUp
import com.CodeNaroNa.vendor.relief.GlobalHelpers.BaseActivity
import com.CodeNaroNa.vendor.relief.VendorActivity.VendorActivity
import com.CodeNaroNa.vendor.relief.Fragments.HomeFragment.Adapters.UserAdapter
import com.CodeNaroNa.vendor.relief.databinding.FragmentHomeBinding
import com.CodeNaroNa.vendor.relief.Models.UserData
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(), OnItemSelectedListener, UserAdapter.UserAdapterEventListener {
    //ViewBinding Variables
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userAdapter: UserAdapter
    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var sharedViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = HomeFragmentViewModelFactory(requireContext().applicationContext, CentralRepository())
        viewModel = ViewModelProvider(this, factory).get(HomeFragmentViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseView()
        setUpLiveDataObservers()
    }

    private fun setUpLiveDataObservers() {
        viewModel.stateList.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    val stateArrayAdapter =
                            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, it.data!!)
                    binding.ustate.adapter = stateArrayAdapter
                }
                else -> {
                }
            }
        })

        viewModel.cityList.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    val cityArrayAdapter =
                            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, it.data!!)
                    binding.ucity.adapter = cityArrayAdapter
                }
                else -> {
                }
            }
        })

        viewModel.vendorList.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    userAdapter.differ.submitList(it.data)
                }
                is Resource.Error -> {
                    (requireActivity() as BaseActivity).showErrorSnackMessage(it.message!!)
                }
                else -> {
                }
            }
        })
    }

    private fun initialiseView() {

        binding.ucity.onItemSelectedListener = this
        binding.ustate.onItemSelectedListener = this
        binding.ucategory.onItemSelectedListener = this

        binding.ucategory.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, viewModel.shopCategoryList)

        binding.Get.setOnClickListener {
            getVendors()
        }

        binding.titleLayout.sign.setOnClickListener {
            //startActivity(Intent(requireActivity(),SignUp::class.java))
            if (FirebaseAuth.getInstance().currentUser != null) {
                val intent = Intent(requireActivity(), VendorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else
                startActivity(Intent(requireActivity(), SignUp::class.java))
        }

        userAdapter = UserAdapter(this)
        binding.recycle.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun getVendors() {
        if (!viewModel.getVendors(binding.ustate.selectedItem?.toString()
                        ?: "", binding.ucity.selectedItem?.toString()
                        ?: "", binding.ucategory.selectedItem.toString()))
            Toast.makeText(requireContext(), "Please Select All Details", Toast.LENGTH_SHORT).show()
    }

    //ViewBinding variable should be set to null to avoid memory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.ustate -> {
                viewModel.getCityList(parent.getItemAtPosition(position).toString())
                binding.ucity.adapter = null
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onUserViewHolderClicked(userData: UserData) {
        sharedViewModel.selectedUserData = userData
        Utility.navigateFragment(requireActivity().supportFragmentManager, R.id.fragment_layout, VendorDataFragment(), "vendorDataFrag")
    }
}