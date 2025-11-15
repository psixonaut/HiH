package com.example.rustoreapplicationshowcases.ui.categories

import androidx.lifecycle.ViewModel
import com.example.rustoreapplicationshowcases.data.repository.AppRepository

class CategoriesViewModel : ViewModel() {

    private val repo = AppRepository

    val categories: List<String> = repo.getAllCategories()
}