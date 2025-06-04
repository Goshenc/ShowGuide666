package com.example.filmguide.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.example.filmguide.IsBuyDialogFragment
import com.example.filmguide.R
import com.example.filmguide.logic.AppDatabase
import com.example.filmguide.logic.network.allperformance.AllPerformanceResponse
import com.example.filmguide.logic.network.performancedetail.PerformanceDetailData
import com.example.filmguide.logic.network.performancedetail.convertToPerformanceEntity
import com.example.filmguide.logic.network.searchperformance.Celebrity
import com.example.filmguide.logic.network.searchperformance.EnhancedPerformance
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup


class DetailPerformanceAdapter(private val context: Context,private val performance: EnhancedPerformance?,private val celebrityList : List<Celebrity>?,private val performanceDetailData: PerformanceDetailData?,private val allPerformanceData : AllPerformanceResponse.PerformanceData?,private val coroutineScope: CoroutineScope) :
    RecyclerView.Adapter<DetailPerformanceAdapter.PerformanceViewHolder>() {


    private val ITEM_HEAD = 0
    private val ITEM_INFO = 1
    private val ITEM_IMAGE = 2
    private val ITEM_BUY = 3


    open class PerformanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HeadViewHolder(itemView: View) : PerformanceViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvArtist: TextView = itemView.findViewById(R.id.tvStarring)
        val tvComingDate: TextView = itemView.findViewById(R.id.tvComingDate)
        val tvPrice : TextView = itemView.findViewById(R.id.tvPrice)
    }

    class BuyViewHolder(itemView: View) : PerformanceViewHolder(itemView) {
        val buyButton : TextView = itemView.findViewById(R.id.buyButton)
    }

    class InfoViewHolder(itemView: View) : PerformanceViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.celebrity_img)
    }

    class ImageViewHolder(itemView: View) : PerformanceViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.performanceImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformanceViewHolder {
        return when (viewType) {
            ITEM_HEAD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_performance_detail_head, parent, false)
                HeadViewHolder(view)
            }
            ITEM_BUY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_performance_detail_buy, parent, false)
                BuyViewHolder(view)
            }
            ITEM_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_performance_detail_img, parent, false)
                ImageViewHolder(view)
            }
            ITEM_INFO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_performance_detail_celebrity, parent, false)
                InfoViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_HEAD
            1 -> ITEM_BUY
            2 -> ITEM_INFO
            3 -> ITEM_IMAGE
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

    override fun getItemCount(): Int = 4

    override fun onBindViewHolder(holder: PerformanceViewHolder, position: Int) {
        val context = holder.itemView.context

        if (performance != null && celebrityList != null) {

            when (holder) {
                is HeadViewHolder -> {
                    Glide.with(context)
                        .load(performance.performance.posterUrl)
                        .into(holder.ivPoster)
                    holder.tvName.text = performance.performance.name
                    if(!celebrityList.isEmpty()) {
                        holder.tvArtist.text =
                            "艺人：${celebrityList.joinToString(" / ") { it.celebrityName }}"
                    } else {
                        holder.tvArtist.text = ""
                    }
                    holder.tvComingDate.text = "日期：${performance.performance.timeRange}"
                    holder.tvPrice.text = "￥" + performance.performance.priceRange
                }

                is BuyViewHolder -> {
                    holder.buyButton.setOnClickListener {
                        val fragmentManager =
                            (context as androidx.fragment.app.FragmentActivity).supportFragmentManager
                        val dialogFragment = IsBuyDialogFragment()
                        dialogFragment.setOnConfirmButtonClickListener(object : IsBuyDialogFragment.OnConfirmButtonClickListener {
                            override fun onConfirmClick() {
                                coroutineScope.launch(Dispatchers.IO) {

                                    val entityPerformance = convertToPerformanceEntity(performance)

                                    val performanceDao = AppDatabase.getInstance(context).performanceDao()
                                    performanceDao.insertPerformance(entityPerformance)

                                    withContext(Dispatchers.Main) {
                                        ToastUtil.show(context, "已自动添加到管理票务页面", R.drawable.icon)
                                    }
                                }
                            }
                        })
                        dialogFragment.show(fragmentManager, "CustomDialogFragment")
                        openUrl("https://www.gewara.com/detail/" + performance.performance.id)
                    }


                }

                is InfoViewHolder -> {
                    if (!celebrityList.isEmpty()) {
                        Glide.with(context)
                            .load(celebrityList[0].headUrl)
                            .into(holder.image)
                    } else {
                        holder.image.scaleType = ImageView.ScaleType.FIT_CENTER
                        Glide.with(context)
                            .load(performance.performance.posterUrl)
                            .into(holder.image)

                    }
                }

                is ImageViewHolder -> {

                    if (performanceDetailData != null && !performanceDetailData.detail.isEmpty()) {
                        val doc = Jsoup.parse(performanceDetailData.detail)
                        val imageUrl = doc.select("img").attr("src")
                        Glide.with(context)
                            .load(imageUrl)
                            .into(holder.image)
                    } else {
                        Glide.with(context)
                            .load(performance.performance.posterUrl)
                            .into(holder.image)

                    }
                }

            }
        } else if (allPerformanceData != null){
            when (holder) {
                is HeadViewHolder -> {
                    Glide.with(context)
                        .load(allPerformanceData.posterUrl)
                        .into(holder.ivPoster)
                    holder.tvName.text = allPerformanceData.name
                    holder.tvArtist.text =
                        "地点：${allPerformanceData.shopName}"
                    holder.tvComingDate.text = "日期：${allPerformanceData.showTimeRange}"
                    holder.tvPrice.text = "￥" + allPerformanceData.priceRange

                }

                is BuyViewHolder -> {
                    holder.buyButton.setOnClickListener {
                        val fragmentManager =
                            (context as androidx.fragment.app.FragmentActivity).supportFragmentManager
                        val dialogFragment = IsBuyDialogFragment()
                        dialogFragment.setOnConfirmButtonClickListener(object : IsBuyDialogFragment.OnConfirmButtonClickListener {
                            override fun onConfirmClick() {
                                coroutineScope.launch(Dispatchers.IO) {

                                    val entityPerformance = convertToPerformanceEntity(allPerformanceData)

                                    val performanceDao = AppDatabase.getInstance(context).performanceDao()
                                    performanceDao.insertPerformance(entityPerformance)

                                    withContext(Dispatchers.Main) {
                                        ToastUtil.show(context, "已自动添加到管理票务页面", R.drawable.icon)
                                    }
                                }
                            }
                        })
                        dialogFragment.show(fragmentManager, "CustomDialogFragment")
                        openUrl("https://www.gewara.com/detail/" + allPerformanceData.performanceId)
                    }


                }

                is InfoViewHolder -> {
                    Glide.with(context)
                        .load(allPerformanceData.posterUrl)
                        .into(holder.image)
                }

                is ImageViewHolder -> {

                    if (performanceDetailData != null && !performanceDetailData.detail.isEmpty()) {
                        val doc = Jsoup.parse(performanceDetailData.detail)
                        val imageUrl = doc.select("img").attr("src")
                        val highResImageUrl = imageUrl.replace("@w_750", "@w_1920")
                        Glide.with(context)
                            .load(highResImageUrl)
                            .override(1080, 1920)
                            .format(DecodeFormat.PREFER_RGB_565)
                            .into(holder.image)
                    } else {
                        holder.image.scaleType = ImageView.ScaleType.FIT_CENTER
                        Glide.with(context)
                            .load(allPerformanceData.posterUrl)
                            .into(holder.image)

                    }
                }

            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent) // 使用传递的 Context
        } else {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
}