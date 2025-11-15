package com.example.rustoreapplicationshowcases.ui.home

import androidx.lifecycle.ViewModel
import com.example.rustoreapplicationshowcases.data.repository.AppRepository
import com.example.rustoreapplicationshowcases.data.model.AppInfo

class HomeViewModel : ViewModel() {

    val apps: List<AppInfo> = AppRepository.apps
}