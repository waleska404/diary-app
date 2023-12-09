package com.waleska404.moodtracker.presentation.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.waleska404.moodtracker.data.repository.Diaries
import com.waleska404.moodtracker.data.repository.MongoDB
import com.waleska404.moodtracker.model.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import java.time.ZonedDateTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class HomeViewModel @Inject constructor(
) : ViewModel() {
    private lateinit var allDiariesJob: Job
    private lateinit var filteredDiariesJob: Job

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeAllDiaries() {
        viewModelScope.launch {
            MongoDB.getAllDiaries().collect { result ->
                diaries.value = result
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeFilteredDiaries(zonedDateTime: ZonedDateTime) {
        filteredDiariesJob = viewModelScope.launch {
            if (::allDiariesJob.isInitialized) {
                allDiariesJob.cancelAndJoin()
            }
            MongoDB.getFilteredDiaries(zonedDateTime = zonedDateTime).collect { result ->
                diaries.value = result
            }
        }
    }

}