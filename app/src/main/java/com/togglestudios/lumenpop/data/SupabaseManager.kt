package com.togglestudios.lumenpop.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val username: String,
    val email: String
)

data class LeaderboardItem(
    val playerId: String,
    val username: String,
    val totalStars: Int,
    val highestLevel: Int,
    val coins: Int
)

class SupabaseManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lumen-pop-supabase-session", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "SupabaseManager"
        const val BASE_URL = "https://syizhueguzpdirnlixej.supabase.co"
        const val ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN5aXpodWVndXpwZGlybmxpeGVqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTE3MDg3NTEsImV4cCI6MjEwNzI4NDc1MX0.mhSEwqCANtl6jVh75bl_mB78HBJQcVXRtCDbJZ4kqEU"

        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    var currentSession: AuthSession? = null
        private set

    init {
        loadSavedSession()
    }

    private fun loadSavedSession() {
        val uid = prefs.getString(KEY_USER_ID, null)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)
        val username = prefs.getString(KEY_USERNAME, null)
        val email = prefs.getString(KEY_EMAIL, null)

        if (!uid.isNullOrEmpty() && !token.isNullOrEmpty() && !username.isNullOrEmpty()) {
            currentSession = AuthSession(
                userId = uid,
                accessToken = token,
                refreshToken = refresh ?: "",
                username = username,
                email = email ?: "$username@lumenpop.app"
            )
        }
    }

    private fun persistSession(session: AuthSession?) {
        currentSession = session
        val editor = prefs.edit()
        if (session != null) {
            editor.putString(KEY_USER_ID, session.userId)
            editor.putString(KEY_ACCESS_TOKEN, session.accessToken)
            editor.putString(KEY_REFRESH_TOKEN, session.refreshToken)
            editor.putString(KEY_USERNAME, session.username)
            editor.putString(KEY_EMAIL, session.email)
        } else {
            editor.clear()
        }
        editor.apply()
    }

    fun getActiveUsername(): String {
        return currentSession?.username ?: "Adventurer"
    }

    fun isLoggedIn(): Boolean {
        return currentSession != null
    }

    suspend fun checkUsernameAvailable(username: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val clean = username.trim().lowercase()
            if (clean.length < 3 || clean.length > 12 || !clean.matches(Regex("^[a-z0-9_-]+$"))) {
                return@withContext Result.success(false)
            }

            val url = URL("$BASE_URL/rest/v1/rpc/check_username_available")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $ANON_KEY")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val payload = JSONObject().put("p_username", clean).toString()
            OutputStreamWriter(conn.outputStream).use { it.write(payload) }

            val code = conn.responseCode
            if (code in 200..299) {
                val respText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText().trim() }
                val isAvailable = respText.equals("true", ignoreCase = true)
                Result.success(isAvailable)
            } else {
                val err = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
                Log.w(TAG, "checkUsernameAvailable failed: $err")
                Result.failure(Exception("Check failed: $code"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkUsernameAvailable error", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithUsername(username: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val clean = username.trim().lowercase()
            val syntheticEmail = "$clean@lumenpop.app"

            val url = URL("$BASE_URL/auth/v1/signup")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val body = JSONObject().apply {
                put("email", syntheticEmail)
                put("password", password)
                put("data", JSONObject().put("username", clean))
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val code = conn.responseCode
            val responseStream = if (code in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream
            val respStr = BufferedReader(InputStreamReader(responseStream)).use { it.readText() }

            if (code in 200..299) {
                val json = JSONObject(respStr)
                var token = json.optString("access_token", "")
                val refresh = json.optString("refresh_token", "")
                val userObj = json.optJSONObject("user") ?: json
                val uid = userObj.optString("id", "")

                // If signup returned without immediate session token, sign in immediately
                if (token.isEmpty()) {
                    val loginRes = signInWithUsername(clean, password)
                    if (loginRes.isSuccess) {
                        return@withContext loginRes
                    }
                }

                val session = AuthSession(
                    userId = uid,
                    accessToken = token,
                    refreshToken = refresh,
                    username = clean,
                    email = syntheticEmail
                )
                persistSession(session)
                Result.success(session)
            } else {
                Log.w(TAG, "SignUp error ($code): $respStr")
                val errMsg = parseErrorMessage(respStr, "Registration failed")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignUp exception", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithUsername(identifier: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val clean = identifier.trim().lowercase()
            val emailToUse = if (clean.contains("@")) clean else "$clean@lumenpop.app"

            val url = URL("$BASE_URL/auth/v1/token?grant_type=password")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val body = JSONObject().apply {
                put("email", emailToUse)
                put("password", password)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val code = conn.responseCode
            val responseStream = if (code in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream
            val respStr = BufferedReader(InputStreamReader(responseStream)).use { it.readText() }

            if (code in 200..299) {
                val json = JSONObject(respStr)
                val token = json.getString("access_token")
                val refresh = json.optString("refresh_token", "")
                val userObj = json.getJSONObject("user")
                val uid = userObj.getString("id")
                val meta = userObj.optJSONObject("user_metadata")
                val username = meta?.optString("username")?.ifEmpty { null }
                    ?: clean.replace("@lumenpop.app", "")

                val session = AuthSession(
                    userId = uid,
                    accessToken = token,
                    refreshToken = refresh,
                    username = username,
                    email = emailToUse
                )
                persistSession(session)
                Result.success(session)
            } else {
                Log.w(TAG, "SignIn error ($code): $respStr")
                val errMsg = parseErrorMessage(respStr, "Invalid username or password")
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn exception", e)
            Result.failure(e)
        }
    }

    suspend fun restoreSession(): Result<AuthSession?> = withContext(Dispatchers.IO) {
        val s = currentSession ?: return@withContext Result.success(null)
        try {
            val url = URL("$BASE_URL/auth/v1/user")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer ${s.accessToken}")

            val code = conn.responseCode
            if (code in 200..299) {
                Result.success(s)
            } else if (code == 401 && s.refreshToken.isNotEmpty()) {
                // Try refresh
                val refreshed = refreshAccessToken(s.refreshToken)
                if (refreshed.isSuccess) {
                    Result.success(refreshed.getOrNull())
                } else {
                    persistSession(null)
                    Result.success(null)
                }
            } else {
                Result.success(s) // Fallback to keeping session if offline
            }
        } catch (e: Exception) {
            // Offline - retain cached session
            Result.success(s)
        }
    }

    private fun refreshAccessToken(refreshToken: String): Result<AuthSession> {
        return try {
            val url = URL("$BASE_URL/auth/v1/token?grant_type=refresh_token")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val body = JSONObject().put("refresh_token", refreshToken)
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val code = conn.responseCode
            if (code in 200..299) {
                val str = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(str)
                val token = json.getString("access_token")
                val newRefresh = json.optString("refresh_token", refreshToken)
                val userObj = json.getJSONObject("user")
                val uid = userObj.getString("id")
                val meta = userObj.optJSONObject("user_metadata")
                val username = meta?.optString("username") ?: currentSession?.username ?: "Adventurer"

                val session = AuthSession(
                    userId = uid,
                    accessToken = token,
                    refreshToken = newRefresh,
                    username = username,
                    email = currentSession?.email ?: ""
                )
                persistSession(session)
                Result.success(session)
            } else {
                Result.failure(Exception("Refresh failed: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        persistSession(null)
    }

    suspend fun loadCloudProgress(userId: String): Result<SavedProgress?> = withContext(Dispatchers.IO) {
        val s = currentSession ?: return@withContext Result.success(null)
        try {
            val url = URL("$BASE_URL/rest/v1/player_progress?player_id=eq.$userId&select=*")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer ${s.accessToken}")

            val code = conn.responseCode
            if (code in 200..299) {
                val respStr = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val arr = JSONArray(respStr)
                if (arr.length() == 0) return@withContext Result.success(null)

                val obj = arr.getJSONObject(0)
                val highest = obj.optInt("highest_unlocked", 1)
                val coins = obj.optInt("coins", 1000)
                val tutorialSeen = obj.optBoolean("tutorial_seen", false)
                val dailyGift = obj.optString("daily_gift_claimed_on", "")
                val completedObj = obj.optJSONObject("completed") ?: JSONObject()

                val completedMap = mutableMapOf<Int, LevelStats>()
                val keys = completedObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val lvl = k.toIntOrNull() ?: continue
                    val statObj = completedObj.getJSONObject(k)
                    val stars = statObj.optInt("stars", 0)
                    val best = statObj.optInt("bestScore", 0)
                    completedMap[lvl] = LevelStats(stars, best)
                }

                val progress = SavedProgress(
                    highestUnlocked = highest,
                    completed = completedMap,
                    coins = coins,
                    username = s.username,
                    tutorialSeen = tutorialSeen,
                    dailyGiftClaimedOn = dailyGift
                )
                Result.success(progress)
            } else {
                Result.failure(Exception("Failed to fetch cloud progress: $code"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadCloudProgress error", e)
            Result.failure(e)
        }
    }

    suspend fun saveCloudProgress(userId: String, progress: SavedProgress): Result<Unit> = withContext(Dispatchers.IO) {
        val s = currentSession ?: return@withContext Result.success(Unit)
        try {
            val url = URL("$BASE_URL/rest/v1/player_progress?player_id=eq.$userId")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer ${s.accessToken}")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "return=representation")
            conn.doOutput = true

            val completedJson = JSONObject()
            progress.completed.forEach { (lvl, stats) ->
                val st = JSONObject().apply {
                    put("stars", stats.stars)
                    put("bestScore", stats.bestScore)
                }
                completedJson.put(lvl.toString(), st)
            }

            val body = JSONObject().apply {
                put("highest_unlocked", progress.highestUnlocked)
                put("coins", progress.coins)
                put("tutorial_seen", progress.tutorialSeen)
                put("daily_gift_claimed_on", progress.dailyGiftClaimedOn)
                put("completed", completedJson)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val code = conn.responseCode
            if (code in 200..299) {
                // Also update leaderboard record asynchronously
                updateLeaderboard(userId, s.username, progress)
                Result.success(Unit)
            } else {
                val err = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
                Log.w(TAG, "saveCloudProgress failed ($code): $err")
                Result.failure(Exception("Save failed: $code"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveCloudProgress error", e)
            Result.failure(e)
        }
    }

    private fun updateLeaderboard(userId: String, username: String, progress: SavedProgress) {
        try {
            val totalStars = progress.completed.values.sumOf { it.stars }
            val url = URL("$BASE_URL/rest/v1/leaderboard?player_id=eq.$userId")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer ${currentSession?.accessToken}")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val body = JSONObject().apply {
                put("username", username)
                put("total_stars", totalStars)
                put("highest_level", progress.highestUnlocked)
                put("coins", progress.coins)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            conn.responseCode
        } catch (e: Exception) {
            Log.w(TAG, "Leaderboard sync skipped: ${e.message}")
        }
    }

    suspend fun fetchLeaderboard(limit: Int = 30): Result<List<LeaderboardItem>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/rest/v1/leaderboard?select=*&order=total_stars.desc,highest_level.desc&limit=$limit")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("apikey", ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $ANON_KEY")

            val code = conn.responseCode
            if (code in 200..299) {
                val str = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val arr = JSONArray(str)
                val list = mutableListOf<LeaderboardItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        LeaderboardItem(
                            playerId = o.optString("player_id", ""),
                            username = o.optString("username", "Stargazer"),
                            totalStars = o.optInt("total_stars", 0),
                            highestLevel = o.optInt("highest_level", 1),
                            coins = o.optInt("coins", 0)
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to load leaderboard: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, username: String): Result<AuthSession> {
        val cleanUser = username.ifBlank { email.substringBefore("@") }
        return signUpWithUsername(cleanUser, password)
    }

    suspend fun signIn(emailOrUsername: String, password: String): Result<AuthSession> {
        return signInWithUsername(emailOrUsername, password)
    }

    suspend fun signInAnonymousOrGuest(preferredUsername: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        val cleanName = preferredUsername.ifBlank { "Stargazer_${(1000..9999).random()}" }
        val generatedPass = "GuestPass_" + System.currentTimeMillis() + "_" + (1000..9999).random()
        val res = signUpWithUsername(cleanName, generatedPass)
        if (res.isSuccess) {
            res
        } else {
            // If already exists, attempt login or local session
            val localSession = AuthSession(
                userId = "guest_" + System.currentTimeMillis(),
                accessToken = ANON_KEY,
                refreshToken = "",
                username = cleanName,
                email = "$cleanName@lumenpop.app"
            )
            persistSession(localSession)
            Result.success(localSession)
        }
    }

    private fun parseErrorMessage(jsonStr: String, defaultMsg: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            obj.optString("msg", obj.optString("message", obj.optString("error_description", defaultMsg)))
        } catch (e: Exception) {
            defaultMsg
        }
    }
}
