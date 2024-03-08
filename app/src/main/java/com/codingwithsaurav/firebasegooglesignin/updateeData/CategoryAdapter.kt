package com.codingwithsaurav.firebasegooglesignin.updateeData

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codingwithsaurav.firebasegooglesignin.R
import com.codingwithsaurav.firebasegooglesignin.databinding.CategoryDataBinding

class CategoryAdapter(
    private val categoryData: ArrayList<CategoryData>,
    private val listener: OnItemClick
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(CategoryDataBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return categoryData.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        with(holder) {
            with(categoryData[position]) {
                if(this.isShow.not()){
                    return
                }
                binding.otherTV.text = this.name
                if (this.isSelected) {
                    binding.categoryIV.borderColor = ContextCompat.getColor(binding.root.context, R.color.primary)
                } else {
                    binding.categoryIV.borderColor = ContextCompat.getColor(binding.root.context, R.color.white)
                }
                binding.rootCL.setOnClickListener {
                    listener.onCategoryClick(this.id, this.name == "Others")
                }
            }
        }
    }

    inner class CategoryViewHolder(val binding: CategoryDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnItemClick {
        fun onCategoryClick(categoryId: String, isAddOther: Boolean)
    }

}