package com.example.filmguide.logic.network.hotmovie


data class HotMovie(
    val id: Int,
    val nm: String, // Movie name
    val cat: String, // Category
    val pubDesc: String, // Release description
    val pubDate: Long, // Publish date (timestamp)
    val rt: String, // Release date in string format
    val img: String, // Image URL
    val sc: Double, // Rating score
    val star: String, // Main actors
    val dir: String, // Director
    val dur: Int, // Duration
    val scm: String, // Subtitle/Content description
    val wish: Int, // Number of people who wish to watch
    val showTimeInfo: String, // Show time info
    val showCinemaNum: Int, // Number of cinemas showing the movie
    val showNum: Int, // Number of shows
    val videoName: String, // Video name
    val videourl: String, // Video URL
    val vnum: Int, // Video views
    val scoreLabel: String // Score label
)

data class HotMoviesResponse(
    val outerData: OuterData // You can adjust the response structure based on your actual API
)

data class OuterData(
    val innerData: InnerData
)

data class InnerData(
    val hot: List<HotMovie> // Now using the existing HotMovie class
)
