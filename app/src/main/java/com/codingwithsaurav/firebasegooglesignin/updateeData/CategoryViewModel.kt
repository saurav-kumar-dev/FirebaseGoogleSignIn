package com.codingwithsaurav.firebasegooglesignin.updateeData

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

class CategoryViewModel : ViewModel() {

    private val _primaryCategoryData = MutableLiveData< ArrayList<CategoryData>>()
    val primaryCategoryData:LiveData<ArrayList<CategoryData>>
        get() = _primaryCategoryData

    private val _secondaryCategoryData = MutableLiveData<ArrayList<CategoryData>>()
    val secondaryCategoryData:LiveData<ArrayList<CategoryData>>
        get() = _secondaryCategoryData


    suspend fun getPrimaryCategoryData(){
        val arrayList = ArrayList<CategoryData>()
        arrayList.add(CategoryData("11233dfdfs", "Sports"))
        arrayList.add(CategoryData("flknsdlfdf", "Cinema"))
        arrayList.add(CategoryData("dskfnsdknf", "Fitness"))
        arrayList.add(CategoryData("1sdfkn4ldf", "Beauty"))
        arrayList.add(CategoryData("sfkn34ldkn", "Home"))
        arrayList.add(CategoryData("324dslkfns", "Arts"))
        arrayList.add(CategoryData("asasfafaf", "Others"))
        delay(30)
        _primaryCategoryData.postValue(arrayList)
    }
    suspend fun getSecondaryCategoryData(){
        val arrayList = ArrayList<CategoryData>()
        arrayList.add(CategoryData("11233dfdfs", "Sports"))
        arrayList.add(CategoryData("flknsdlfdf", "Cinema"))
        arrayList.add(CategoryData("dskfnsdknf", "Fitness"))
        arrayList.add(CategoryData("1sdfkn4ldf", "Beauty"))
        arrayList.add(CategoryData("sfkn34ldkn", "Home"))
        arrayList.add(CategoryData("324dslkfns", "Arts"))
        delay(30)
        _secondaryCategoryData.postValue(arrayList)
    }
}