/*
 * Copyright (c) 2022 Kodeco Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 * 
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.yourcompany.android.jetreddit.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.android.jetreddit.data.repository.Repository
import com.yourcompany.android.jetreddit.domain.model.PostModel
import com.yourcompany.android.jetreddit.screens.communities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
  private val repository: Repository,
  private val dataStore: DataStore<Preferences>
) : ViewModel() {

  val allPosts by lazy { repository.getAllPosts() }

  val myPosts by lazy { repository.getAllOwnedPosts() }

  val subreddits by lazy { MutableLiveData<List<String>>() }

  val subredditsToggle: Flow<Map<Int, Boolean>> = dataStore.data
    .map { preferences ->
      communities.associateWith { communityId ->
        val prefValue = preferences[booleanPreferencesKey(communityId.toString())] ?: false
        prefValue
      }
    }

  val selectedCommunity: MutableLiveData<String> by lazy { MutableLiveData<String>() }

  fun searchCommunities(searchedText: String) {
    viewModelScope.launch(Dispatchers.Default) {
      subreddits.postValue(repository.getAllSubreddits(searchedText))
    }
  }

  fun savePost(post: PostModel) {
    viewModelScope.launch(Dispatchers.Default) {
      repository.insert(post.copy(subreddit = selectedCommunity.value ?: ""))
    }
  }

  fun toggleSubreddit(value: Boolean, subredditId: Int) {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.edit {
        it[booleanPreferencesKey(subredditId.toString())] = value
      }
    }
  }
}