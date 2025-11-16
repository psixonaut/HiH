package com.example.rustoreapplicationshowcases.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import com.example.rustoreapplicationshowcases.data.repository.AppRepository
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import android.util.Log

class CategorySelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)
    private val preferencesManager = PreferencesManager(application)

    val allCategories: List<String> = run {
        val categories = repo.getAllCategories()
        Log.d("CategorySelectionViewModel", "Loaded ${categories.size} categories")
        categories
    }

    val selectedCategories = mutableStateListOf<String>()

    init {
        // Load saved categories from preferences
        val savedCategories = preferencesManager.getSelectedCategories()
        if (savedCategories != null && savedCategories.isNotBlank()) {
            setSelectedCategoriesFromString(savedCategories)
            Log.d("CategorySelectionViewModel", "Loaded ${selectedCategories.size} saved categories")
        }
    }

    fun toggleCategorySelection(category: String) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
        } else {
            if (selectedCategories.size < 10) {
                selectedCategories.add(category)
            }
        }
        // Save categories after each change
        saveSelectedCategories()
    }
    
    private fun saveSelectedCategories() {
        val categoriesString = getSelectedCategoriesAsString()
        preferencesManager.setSelectedCategories(categoriesString)
        Log.d("CategorySelectionViewModel", "Saved categories: $categoriesString")
    }

    fun getSelectedCategoriesAsString(): String {
        return selectedCategories.joinToString(",")
    }

    fun setSelectedCategoriesFromString(categoriesString: String?) {
        selectedCategories.clear()
        categoriesString?.split(",")?.filter { it.isNotBlank() }?.let { newCategories ->
            selectedCategories.addAll(newCategories.take(10))
        }
    }
    
    fun moveCategoryUp(index: Int) {
        if (index > 0 && index < selectedCategories.size) {
            val category = selectedCategories.removeAt(index)
            selectedCategories.add(index - 1, category)
            saveSelectedCategories()
        }
    }
    
    fun moveCategoryDown(index: Int) {
        if (index >= 0 && index < selectedCategories.size - 1) {
            val category = selectedCategories.removeAt(index)
            selectedCategories.add(index + 1, category)
            saveSelectedCategories()
        }
    }
    
    fun moveCategory(fromIndex: Int, toIndex: Int) {
        if (fromIndex in selectedCategories.indices && toIndex in selectedCategories.indices && fromIndex != toIndex) {
            val category = selectedCategories.removeAt(fromIndex)
            selectedCategories.add(toIndex, category)
            saveSelectedCategories()
        }
    }
}
