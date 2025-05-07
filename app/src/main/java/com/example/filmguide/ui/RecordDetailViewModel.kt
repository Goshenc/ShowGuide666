package com.example.filmguide.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.filmguide.logic.recordroom.RecordDatabase
import com.example.filmguide.logic.recordroom.RecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordDetailViewModel (application: Application) : AndroidViewModel(application) {
    private val diaryDao = RecordDatabase.getInstance(application).recordDao()
    private val _diaryEntity = MutableLiveData<RecordEntity?>()
    val diaryEntity: LiveData<RecordEntity?> get() = _diaryEntity

    fun loadDiaryDetails(diaryId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val diary = diaryDao.getRecordById(diaryId)
            _diaryEntity.postValue(diary) // 更新 LiveData，通知 UI 更新
        }
    }
}