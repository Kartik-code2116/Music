package com.example.music.data.api

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://cizbkhhbufimvqswvwsh.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNpemJraGhidWZpbXZxc3d2d3NoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE2NTgxMTcsImV4cCI6MjA4NzIzNDExN30.Jjzn_ErXLyu9R0FjUUBNPwGwYP2I32lepk7kZ7zlNaQ"
    ) {
        install(Postgrest)
        install(Storage)
    }
}
