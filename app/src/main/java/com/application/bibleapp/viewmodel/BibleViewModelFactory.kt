package com.application.bibleapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.application.bibleapp.data.model.BibleSourceType
import com.application.bibleapp.data.model.SelectedBibleVersion
import com.application.bibleapp.data.remote.HelloAoBibleDataSource
import com.application.bibleapp.data.remote.OurMannaBibleDataSource
import com.application.bibleapp.data.repository.BibleRepository

class BibleViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BibleViewModel::class.java)) {

            val repository = BibleRepository(
                context = context,
                remote = HelloAoBibleDataSource(),
                daily = OurMannaBibleDataSource()
            )

            val viewModel = BibleViewModel(repository)

            @Suppress("UNCHECKED_CAST")
            return viewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}