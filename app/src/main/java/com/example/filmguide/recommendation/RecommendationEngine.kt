package com.example.filmguide.recommendation

import android.content.Context
import com.example.filmguide.logic.recordroom.RecordDatabase
import com.example.filmguide.logic.recordroom.RecordEntity
import com.example.filmguide.network.MovieDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * 智能推荐引擎
 * 基于用户历史记录和偏好进行个性化推荐
 */
class RecommendationEngine(private val context: Context) {
    
    private val database = RecordDatabase.getInstance(context)
    private val movieDataService = MovieDataService(context)
    
    /**
     * 用户偏好标签
     */
    data class UserPreference(
        val genres: MutableSet<String> = mutableSetOf(),
        val actors: MutableSet<String> = mutableSetOf(),
        val directors: MutableSet<String> = mutableSetOf(),
        val locations: MutableSet<String> = mutableSetOf(),
        val timeSlots: MutableSet<String> = mutableSetOf()
    )
    
    /**
     * 推荐结果
     */
    data class Recommendation(
        val title: String,
        val reason: String,
        val confidence: Float,
        val type: RecommendationType
    )
    
    enum class RecommendationType {
        MOVIE, CONCERT, EXHIBITION, ACTIVITY
    }
    
    /**
     * 分析用户偏好
     */
    suspend fun analyzeUserPreferences(): UserPreference = withContext(Dispatchers.IO) {
        val records = database.recordDao().getRecords().value ?: emptyList()
        val preference = UserPreference()
        
        records.forEach { record: RecordEntity ->
            // 从标题中提取类型信息
            extractGenres(record.title, preference)
            extractActors(record.title, preference)
            extractLocation(record.location, preference)
            extractTimeSlot(record.date, preference)
        }
        
        preference
    }
    
    /**
     * 生成个性化推荐
     */
    suspend fun generateRecommendations(): List<Recommendation> = withContext(Dispatchers.IO) {
        val preferences = analyzeUserPreferences()
        val recommendations = mutableListOf<Recommendation>()
        
        // 基于用户偏好的智能推荐
        recommendations.addAll(generateSmartMovieRecommendations(preferences))
        
        // 基于偏好的活动推荐
        recommendations.addAll(generateActivityRecommendations(preferences))
        
        // 基于地理位置的推荐
        recommendations.addAll(generateLocationBasedRecommendations(preferences))
        
        // 如果没有用户数据，提供热门推荐
        if (recommendations.isEmpty()) {
            recommendations.addAll(generatePopularRecommendations())
        }
        
        recommendations.sortedByDescending { it.confidence }
    }
    
    /**
     * 基于用户选择的类型生成推荐
     */
    suspend fun generateRecommendationsByGenres(selectedGenres: List<String>): List<Recommendation> = withContext(Dispatchers.IO) {
        val recommendations = mutableListOf<Recommendation>()
        
        // 为每个选择的类型获取电影推荐
        selectedGenres.forEach { genre ->
            val movies = movieDataService.getMoviesByGenre(genre)
            movies.take(5).forEach { movie ->
                recommendations.add(Recommendation(
                    movie.title,
                    "基于您对${genre}类型电影的喜爱，为您推荐这部${movie.rating}分的高分作品：${movie.description}",
                    0.90f + (movie.rating / 10.0).toFloat(),
                    RecommendationType.MOVIE
                ))
            }
        }
        
        // 如果推荐不够，添加一些热门电影
        if (recommendations.size < 5) {
            val hotMovies = movieDataService.getHotMovies()
            hotMovies.take(3).forEach { movie ->
                if (!recommendations.any { it.title == movie.title }) {
                    recommendations.add(Recommendation(
                        movie.title,
                        "热门电影推荐：${movie.description}，评分${movie.rating}分",
                        0.80f + (movie.rating / 10.0).toFloat(),
                        RecommendationType.MOVIE
                    ))
                }
            }
        }
        
        recommendations.sortedByDescending { it.confidence }
    }
    
    /**
     * 智能电影推荐 - 基于真实数据
     */
    private suspend fun generateSmartMovieRecommendations(preferences: UserPreference): List<Recommendation> = withContext(Dispatchers.IO) {
        val recommendations = mutableListOf<Recommendation>()
        
        // 如果用户有明确的类型偏好，获取对应类型的电影
        if (preferences.genres.isNotEmpty()) {
            preferences.genres.forEach { genre ->
                val movies = movieDataService.getMoviesByGenre(genre)
                movies.take(2).forEach { movie ->
                    recommendations.add(Recommendation(
                        movie.title,
                        "基于您对${genre}类型电影的喜爱，为您推荐这部${movie.rating}分的高分作品",
                        0.85f + (movie.rating / 10.0).toFloat(),
                        RecommendationType.MOVIE
                    ))
                }
            }
        } else {
            // 如果没有明确偏好，推荐热门电影
            val hotMovies = movieDataService.getHotMovies()
            hotMovies.take(3).forEach { movie ->
                recommendations.add(Recommendation(
                    movie.title,
                    "热门电影推荐：${movie.description}，评分${movie.rating}分",
                    0.80f + (movie.rating / 10.0).toFloat(),
                    RecommendationType.MOVIE
                ))
            }
        }
        
        // 推荐即将上映的电影
        val upcomingMovies = movieDataService.getUpcomingMovies()
        upcomingMovies.take(1).forEach { movie ->
            recommendations.add(Recommendation(
                movie.title,
                "即将上映：${movie.description}，${movie.director}导演作品",
                0.75f,
                RecommendationType.MOVIE
            ))
        }
        
        recommendations
    }
    
    private fun generateMovieRecommendations(preferences: UserPreference): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // 基于类型推荐
        preferences.genres.forEach { genre ->
            when (genre.lowercase()) {
                "动作", "action" -> {
                    recommendations.add(Recommendation(
                        "《速度与激情10》",
                        "基于您对动作片的喜爱",
                        0.85f,
                        RecommendationType.MOVIE
                    ))
                }
                "爱情", "romance" -> {
                    recommendations.add(Recommendation(
                        "《泰坦尼克号》重映",
                        "经典爱情电影，符合您的喜好",
                        0.90f,
                        RecommendationType.MOVIE
                    ))
                }
                "科幻", "sci-fi" -> {
                    recommendations.add(Recommendation(
                        "《阿凡达3》",
                        "科幻大片，值得期待",
                        0.88f,
                        RecommendationType.MOVIE
                    ))
                }
            }
        }
        
        return recommendations
    }
    
    private fun generateActivityRecommendations(preferences: UserPreference): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // 基于时间偏好的活动推荐
        preferences.timeSlots.forEach { timeSlot ->
            when (timeSlot) {
                "周末" -> {
                    recommendations.add(Recommendation(
                        "周末市集活动",
                        "适合周末的休闲活动",
                        0.75f,
                        RecommendationType.ACTIVITY
                    ))
                }
                "晚上" -> {
                    recommendations.add(Recommendation(
                        "夜间音乐会",
                        "晚间娱乐活动",
                        0.80f,
                        RecommendationType.CONCERT
                    ))
                }
            }
        }
        
        return recommendations
    }
    
    private fun generateLocationBasedRecommendations(preferences: UserPreference): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        preferences.locations.forEach { location ->
            recommendations.add(Recommendation(
                "$location 附近的新上映电影",
                "基于您常去的${location}地区",
                0.70f,
                RecommendationType.MOVIE
            ))
        }
        
        return recommendations
    }
    
    /**
     * 生成热门推荐（当用户没有历史数据时）- 从猫眼获取真实数据
     */
    private suspend fun generatePopularRecommendations(): List<Recommendation> = withContext(Dispatchers.IO) {
        try {
            // 从猫眼获取真实的热门电影数据
            val hotMovies = movieDataService.getHotMovies()
            val recommendations = mutableListOf<Recommendation>()
            
            hotMovies.forEach { movie ->
                recommendations.add(Recommendation(
                    movie.title,
                    "热门电影推荐：${movie.description}，评分${movie.rating}分",
                    0.80f + (movie.rating / 10.0).toFloat(),
                    RecommendationType.MOVIE
                ))
            }
            
            // 如果热门电影不够，添加即将上映的电影
            if (recommendations.size < 5) {
                val upcomingMovies = movieDataService.getUpcomingMovies()
                upcomingMovies.take(3).forEach { movie ->
                    recommendations.add(Recommendation(
                        movie.title,
                        "即将上映：${movie.description}，${movie.director}导演作品",
                        0.75f,
                        RecommendationType.MOVIE
                    ))
                }
            }
            
            recommendations
        } catch (e: Exception) {
            // 如果获取真实数据失败，返回空列表
            emptyList()
        }
    }
    
    private fun extractGenres(title: String, preference: UserPreference) {
        val genreKeywords = mapOf(
            "动作" to listOf("动作", "枪战", "爆炸", "追车", "速度", "激情", "碟中谍", "007", "复仇者", "蜘蛛侠", "蝙蝠侠"),
            "爱情" to listOf("爱情", "浪漫", "恋爱", "情侣", "你的名字", "铃芽", "泰坦尼克", "罗密欧", "朱丽叶"),
            "科幻" to listOf("科幻", "未来", "机器人", "太空", "阿凡达", "流浪地球", "星际", "银河", "星球大战", "变形金刚"),
            "喜剧" to listOf("喜剧", "搞笑", "幽默", "开心", "你好", "李焕英", "独行月球", "夏洛特", "西虹市"),
            "恐怖" to listOf("恐怖", "惊悚", "鬼", "吓人", "咒", "招魂", "寂静岭", "午夜凶铃"),
            "动画" to listOf("动画", "动漫", "卡通", "皮克斯", "迪士尼", "宫崎骏", "新海诚", "马里奥", "蜘蛛侠"),
            "悬疑" to listOf("悬疑", "推理", "侦探", "满江红", "唐人街", "神探", "无间道"),
            "传记" to listOf("传记", "奥本海默", "传记片", "真实故事", "历史人物")
        )
        
        genreKeywords.forEach { (genre, keywords) ->
            if (keywords.any { title.contains(it, ignoreCase = true) }) {
                preference.genres.add(genre)
            }
        }
    }
    
    private fun extractActors(title: String, preference: UserPreference) {
        // 这里可以集成演员数据库
        val commonActors = listOf("成龙", "周星驰", "刘德华", "周润发", "梁朝伟")
        commonActors.forEach { actor ->
            if (title.contains(actor)) {
                preference.actors.add(actor)
            }
        }
    }
    
    private fun extractLocation(location: String?, preference: UserPreference) {
        location?.let {
            preference.locations.add(it)
        }
    }
    
    private fun extractTimeSlot(date: String?, preference: UserPreference) {
        // 这里可以根据日期判断时间段偏好
        // 简化实现，实际可以更复杂
        preference.timeSlots.add("周末")
        preference.timeSlots.add("晚上")
    }
}
