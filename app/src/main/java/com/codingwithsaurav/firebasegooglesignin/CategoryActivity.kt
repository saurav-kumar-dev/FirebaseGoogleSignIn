package com.codingwithsaurav.firebasegooglesignin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingwithsaurav.firebasegooglesignin.updateeData.CategoryFragment

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        supportFragmentManager.beginTransaction().add(R.id.container, CategoryFragment()).commit()
    }

}