package com.codingwithsaurav.firebasegooglesignin.updateeData

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.codingwithsaurav.firebasegooglesignin.databinding.FragmentCategoryBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.codingwithsaurav.firebasegooglesignin.R
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var binding: FragmentCategoryBinding? = null
    private val categoryViewModel by viewModels<CategoryViewModel>()
    private var primaryCategoryAdapter: CategoryAdapter? = null
    private var secondaryCategoryAdapter: CategoryAdapter? = null
    private var primaryCategoryList = ArrayList<CategoryData>()
    private var secondaryCategoryList = ArrayList<CategoryData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            categoryViewModel.getPrimaryCategoryData()
            categoryViewModel.getSecondaryCategoryData()
        }
        observeCategoryData()
    }

    private fun observeCategoryData() {
        with(categoryViewModel) {
            primaryCategoryData.observe(viewLifecycleOwner) { data ->
                primaryCategoryList.clear()
                primaryCategoryList.addAll(data)
                primaryCategoryAdapter = CategoryAdapter(primaryCategoryList, primaryCategoryClick)
                binding?.primaryCategoryRV?.adapter = primaryCategoryAdapter
            }
            secondaryCategoryData.observe(viewLifecycleOwner) { data ->
                secondaryCategoryList.clear()
                secondaryCategoryList.addAll(data)
                secondaryCategoryAdapter = CategoryAdapter(secondaryCategoryList, secondaryCategoryClick)
                binding?.secondaryCategoryRV?.adapter = secondaryCategoryAdapter
            }
        }
    }


    private val primaryCategoryClick = object : CategoryAdapter.OnItemClick {
        override fun onCategoryClick(categoryId: String, isAddOther: Boolean) {
            binding?.otherTV?.isVisible = isAddOther
            binding?.otherET?.isVisible = isAddOther
            if (isAddOther) {
                primaryCategoryList.forEach { it.isSelected = false }
                secondaryCategoryList.forEach { it.isShow = true }
            } else {
                primaryCategoryList.forEach { it.isSelected = it.id  == categoryId}
                secondaryCategoryList.forEach { it.isShow = it.id != categoryId }
            }
            secondaryCategoryAdapter?.notifyDataSetChanged()
            primaryCategoryAdapter?.notifyDataSetChanged()
        }
    }


    private val secondaryCategoryClick = object : CategoryAdapter.OnItemClick {
        override fun onCategoryClick(categoryId: String, isAddOther: Boolean) {
            secondaryCategoryList.forEach { it.isSelected = it.id  == categoryId}
            secondaryCategoryAdapter?.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Hello", Toast.LENGTH_SHORT).show()
        }
    }

}