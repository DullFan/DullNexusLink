package com.dullfan.nexuslink.utils.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun ViewModel.launchIO(block: suspend CoroutineScope.() -> Unit): Job {
    return viewModelScope.launch(Dispatchers.IO, block = block)
}

fun ViewModel.launchMain(block: suspend CoroutineScope.() -> Unit):Job {
    return viewModelScope.launch(Dispatchers.Main, block = block)
}
