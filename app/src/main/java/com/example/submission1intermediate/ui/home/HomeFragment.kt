package com.example.submission1intermediate.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.submission1intermediate.MainActivity
import com.example.submission1intermediate.data.local.entity.StoryEntity
import com.example.submission1intermediate.databinding.FragmentHomeBinding
import com.example.submission1intermediate.ui.StoryAdapter
import com.example.submission1intermediate.ui.addstory.AddStory
import com.example.submission1intermediate.ui.login.AuthActivity
import com.example.submission1intermediate.utils.ViewModelFactory
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class HomeFragment : Fragment(), KodeinAware {

    override val kodein: Kodein by kodein()
    private val viewModelFactory: ViewModelFactory by instance()
    private var token: String = ""
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private lateinit var listStoryAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel= ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return  binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = requireActivity().intent.getStringExtra(MainActivity.EXTRA_TOKEN) ?:"token null"

        setSwipeRefreshLayout()
        setRecyclerView()
        getStory()

        binding?.btnAddStory?.setOnClickListener {
            Intent(requireContext(), AddStory::class.java).also { intent ->
                startActivity(intent)
            }
        }


        binding?.btnLogout?.setOnClickListener {
            homeViewModel.saveAuthToken("")
            val intent =  Intent(requireContext(), AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        getStory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun getStory(){
        binding?.swipeRefresh?.isRefreshing = true

        homeViewModel.getAllStory(token).observe(viewLifecycleOwner){result ->
            updateRecyclerViewData(result)
            binding?.swipeRefresh?.isRefreshing = false
        }

    }

    private fun setRecyclerView(){
        val linearLayoutManager = LinearLayoutManager(requireContext())
        listStoryAdapter = StoryAdapter()


        recyclerView = binding?.rvStory!!
        recyclerView.apply {
            adapter = listStoryAdapter
            layoutManager = linearLayoutManager
        }

    }

    private fun updateRecyclerViewData(story: PagingData<StoryEntity>){

        val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
        listStoryAdapter.submitData(lifecycle, story)
        recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun setSwipeRefreshLayout(){
        binding?.swipeRefresh?.setOnRefreshListener {
            getStory()
        }
    }




}