package com.example.rustoreapplicationshowcases

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rustoreapplicationshowcases.ui.categories.CategoriesViewModel
import com.example.rustoreapplicationshowcases.ui.details.DetailsViewModel
import com.example.rustoreapplicationshowcases.ui.home.HomeViewModel

class AppViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(app) as T

            modelClass.isAssignableFrom(CategoriesViewModel::class.java) ->
                CategoriesViewModel(app) as T

            modelClass.isAssignableFrom(DetailsViewModel::class.java) ->
                DetailsViewModel(app) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}