package com.nate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nate.app.data.local.PreferencesManager
import com.nate.app.data.repository.MediaRepository
import com.nate.app.util.NetworkMonitor

class NateViewModelFactory(
    private val repository: MediaRepository,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: NetworkMonitor,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository, networkMonitor) as T
            modelClass.isAssignableFrom(MoviesViewModel::class.java) ->
                MoviesViewModel(repository, networkMonitor) as T
            modelClass.isAssignableFrom(TvShowsViewModel::class.java) ->
                TvShowsViewModel(repository, networkMonitor) as T
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(repository, networkMonitor) as T
            modelClass.isAssignableFrom(DetailsViewModel::class.java) ->
                DetailsViewModel(repository, preferencesManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
