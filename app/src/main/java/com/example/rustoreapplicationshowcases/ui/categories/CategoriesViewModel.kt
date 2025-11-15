package com.example.rustoreapplicationshowcases.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rustoreapplicationshowcases.data.repository.AppRepository

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)

    val categories: List<String> = repo.getAllCategories()
}