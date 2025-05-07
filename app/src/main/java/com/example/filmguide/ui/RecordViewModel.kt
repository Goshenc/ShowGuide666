package com.example.filmguide.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.filmguide.logic.recordroom.RecordDatabase
import com.example.filmguide.logic.recordroom.RecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val diaryDao = RecordDatabase.getInstance(application).recordDao()

    val allDiaries: LiveData<List<RecordEntity>> = diaryDao.getRecords()

    fun deleteDiary(diaryEntity: RecordEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            diaryDao.deleteRecord(diaryEntity)
        }
    }

    fun searchDiaries(query: String): LiveData<List<RecordEntity>> {

        return diaryDao.searchRecords("%$query%")
    }
}
