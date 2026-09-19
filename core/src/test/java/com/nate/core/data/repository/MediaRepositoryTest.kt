package com.nate.core.data.repository

import com.nate.core.data.api.MediaApiService
import com.nate.core.data.api.RetryInterceptor
import com.nate.core.data.local.FavoritesDao
import com.nate.core.data.local.PlaybackResumeDao
import com.nate.core.data.local.WatchHistoryDao
import com.nate.core.data.model.CategoryDto
import com.nate.core.data.model.MediaItemDto
import com.nate.core.data.model.StreamInfoDto
import com.nate.core.data.model.StreamLinkDto
import com.nate.core.data.model.SubtitleLinkDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: MediaApiService
    private lateinit var repository: MediaRepositoryImpl

    private val watchHistoryDao = mock(WatchHistoryDao::class.java)
    private val favoritesDao = mock(FavoritesDao::class.java)
    private val playbackResumeDao = mock(PlaybackResumeDao::class.java)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create Retrofit client targeting MockWebServer
        val client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxRetries = 2, initialDelayMs = 10)) // low retry delay for tests
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MediaApiService::class.java)

        repository = MediaRepositoryImpl(
            apiService = apiService,
            watchHistoryDao = watchHistoryDao,
            favoritesDao = favoritesDao,
            playbackResumeDao = playbackResumeDao
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetCategoriesMapsToDomainCorrectly() = runTest {
        // Enqueue Mock JSON Response
        val mockJson = """
            [
              {
                "id": "action",
                "name": "Action Movies",
                "items": [
                  {
                    "tmdbId": "550",
                    "title": "Fight Club",
                    "overview": "An insomniac office worker...",
                    "posterPath": "/poster.jpg",
                    "backdropPath": "/backdrop.jpg",
                    "releaseYear": "1999",
                    "genres": ["Drama"],
                    "rating": 8.4,
                    "runtime": "139 min",
                    "mediaType": "movie"
                  }
                ]
              }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        val categories = repository.getCategories()

        assertEquals(1, categories.size)
        assertEquals("action", categories[0].id)
        assertEquals("Action Movies", categories[0].name)
        assertEquals(1, categories[0].items.size)
        assertEquals("550", categories[0].items[0].tmdbId)
        assertEquals("Fight Club", categories[0].items[0].title)
    }

    @Test
    fun testRetryInterceptorSuccessAfterOneFailure() = runTest {
        val mockJson = """
            {
              "tmdbId": "550",
              "title": "Fight Club",
              "streams": [
                {
                  "serverId": "server_1",
                  "serverName": "Server 1",
                  "url": "https://server1.com/stream.m3u8",
                  "format": "HLS"
                }
              ],
              "subtitles": []
            }
        """.trimIndent()

        // Enqueue 503 Server Error, then 200 Success
        mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody("Service Unavailable"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        val streamInfo = repository.getStreamInfo("550", "movie", null, null)

        assertEquals("550", streamInfo.tmdbId)
        assertEquals(1, streamInfo.streams.size)
        assertEquals("server_1", streamInfo.streams[0].serverId)
        assertEquals("https://server1.com/stream.m3u8", streamInfo.streams[0].url)
    }
}
