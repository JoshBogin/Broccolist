package com.broccolist

import androidx.lifecycle.ViewModel
import io.github.jan.supabase.SupabaseClient

class MainViewModel(val supabaseClient: SupabaseClient) : ViewModel()
