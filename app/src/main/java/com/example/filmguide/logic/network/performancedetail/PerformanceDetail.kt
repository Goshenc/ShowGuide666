package com.example.filmguide.logic.network.performancedetail



import com.google.gson.annotations.SerializedName

data class PerformanceDetailResponse(
    val code: Int,
    val msg: String,
    val data: PerformanceDetailData?,
    val paging: Any?, // 根据JSON数据，paging为null，用Any?表示
    @SerializedName("attrMaps") val attrMaps: AttrMaps,
    val success: Boolean
)

data class PerformanceDetailData(
    @SerializedName("performanceId") val performanceId: Long,
    @SerializedName("ticketStatus") val ticketStatus: Int,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("shopId") val shopId: String,
    val name: String,
    @SerializedName("shopName") val shopName: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("postUrlForShare") val postUrlForShare: String,
    @SerializedName("showTimeRange") val showTimeRange: String,
    @SerializedName("seatUrl") val seatUrl: String,
    @SerializedName("performanceLabelVO") val performanceLabelVO: PerformanceLabelVO,
    val detail: String,
    @SerializedName("ticketNotes") val ticketNotes: String,
    @SerializedName("cityId") val cityId: Int,
    @SerializedName("cityName") val cityName: String,
    @SerializedName("lowestPrice") val lowestPrice: String,
    @SerializedName("seatType") val seatType: Int,
    @SerializedName("saleRemindVO") val saleRemindVO: SaleRemindVO,
    @SerializedName("boostSaleRemindVO") val boostSaleRemindVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("shareLink") val shareLink: String,
    @SerializedName("stockOutRegister") val stockOutRegister: Int,
    @SerializedName("needAnswer") val needAnswer: Int,
    @SerializedName("questionBankId") val questionBankId: Int,
    @SerializedName("questionHint") val questionHint: Any?, // JSON中为null，用Any?表示
    @SerializedName("poi") val poi: Any?, // JSON中为null，用Any?表示
    val self: Boolean,
    @SerializedName("thirdPartyDesc") val thirdPartyDesc: Any?, // JSON中为null，用Any?表示
    @SerializedName("expressSupported") val expressSupported: Boolean,
    @SerializedName("kingHonorFlag") val kingHonorFlag: Boolean,
    @SerializedName("kingHonorParam") val kingHonorParam: Any?, // JSON中为null，用Any?表示
    @SerializedName("generalAgent") val generalAgent: Int,
    @SerializedName("exclusive") val exclusive: Int,
    @SerializedName("saleStatus") val saleStatus: Int,
    @SerializedName("travelGuide") val travelGuide: Boolean,
    @SerializedName("promotional") val promotional: Promotional,
    @SerializedName("priceList") val priceList: List<String>,
    @SerializedName("sellPriceList") val sellPriceList: List<String>,
    @SerializedName("needRemindPrice") val needRemindPrice: Boolean,
    @SerializedName("ticketPriceList") val ticketPriceList: Any?, // JSON中为null，用Any?表示
    @SerializedName("currentLimit") val currentLimit: Boolean,
    @SerializedName("modelStyle") val modelStyle: Int,
    @SerializedName("only") val only: Boolean,
    @SerializedName("ticketRobbingProject") val ticketRobbingProject: Boolean,
    @SerializedName("projectVideoVO") val projectVideoVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("isAssemble") val isAssemble: Boolean,
    @SerializedName("projectDetailAssembleVO") val projectDetailAssembleVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("priceRangeType") val priceRangeType: Int,
    @SerializedName("preview") val preview: Boolean,
    @SerializedName("shareTitle") val shareTitle: String,
    @SerializedName("needCloseDetail") val needCloseDetail: Boolean,
    @SerializedName("needCloseRecommend") val needCloseRecommend: Boolean,
    @SerializedName("brightPointList") val brightPointList: List<Any>, // JSON中为空列表，用List<Any>表示
    @SerializedName("photoStageList") val photoStageList: List<Any>, // JSON中为空列表，用List<Any>表示
    @SerializedName("videoList") val videoList: List<Any>, // JSON中为空列表，用List<Any>表示
    @SerializedName("musicList") val musicList: Any?, // JSON中为null，用Any?表示
    @SerializedName("serviceTitleList") val serviceTitleList: List<ServiceTitle>,
    @SerializedName("buttonToast") val buttonToast: String,
    @SerializedName("brightName") val brightName: String,
    @SerializedName("videoStageName") val videoStageName: String,
    @SerializedName("videoStagePhotoTotal") val videoStagePhotoTotal: Int,
    @SerializedName("videoNum") val videoNum: Int,
    @SerializedName("photoNum") val photoNum: Int,
    @SerializedName("primaryOrgId") val primaryOrgId: Any?, // JSON中为null，用Any?表示
    @SerializedName("projectReserveVO") val projectReserveVO: ProjectReserveVO,
    @SerializedName("isHotProject") val isHotProject: Boolean,
    @SerializedName("prepaySale") val prepaySale: Any?, // JSON中为null，用Any?表示
    @SerializedName("needRealName") val needRealName: Boolean,
    @SerializedName("projectServing") val projectServing: Any?, // JSON中为null，用Any?表示
    @SerializedName("activityType") val activityType: Int,
    @SerializedName("memberInfo") val memberInfo: Any?, // JSON中为null，用Any?表示
    @SerializedName("openMemDiscount") val openMemDiscount: Int,
    @SerializedName("projectHandleVO") val projectHandleVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("relievedTagVO") val relievedTagVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("tpId") val tpId: Int,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("couponMaxDiscount") val couponMaxDiscount: Double,
    @SerializedName("tags") val tags: Any?, // JSON中为null，用Any?表示
    @SerializedName("startTime") val startTime: Any?, // JSON中为null，用Any?表示
    @SerializedName("endTime") val endTime: Any?, // JSON中为null，用Any?表示
    @SerializedName("needFaceCheck") val needFaceCheck: Boolean,
    @SerializedName("multipleReward") val multipleReward: Any?, // JSON中为null，用Any?表示
    @SerializedName("showMemReward") val showMemReward: Any?, // JSON中为null，用Any?表示
    @SerializedName("allowHKDemo") val allowHKDemo: Boolean,
    @SerializedName("playerMinNumLimit") val playerMinNumLimit: Any?, // JSON中为null，用Any?表示
    @SerializedName("playerMaxNumLimit") val playerMaxNumLimit: Any?, // JSON中为null，用Any?表示
    @SerializedName("normalTags") val normalTags: Any?, // JSON中为null，用Any?表示
    @SerializedName("playTime") val playTime: Any?, // JSON中为null，用Any?表示
    @SerializedName("theatreDetail") val theatreDetail: Any?, // JSON中为null，用Any?表示
    @SerializedName("casts") val casts: Any?, // JSON中为null，用Any?表示
    @SerializedName("saleType") val saleType: Any?, // JSON中为null，用Any?表示
    @SerializedName("earlyOnSaleDay") val earlyOnSaleDay: Any?, // JSON中为null，用Any?表示
    @SerializedName("earlyOffSaleMinute") val earlyOffSaleMinute: Any?, // JSON中为null，用Any?表示
    @SerializedName("newTemplate") val newTemplate: Int,
    @SerializedName("billBoardVO") val billBoardVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("discountPriceStr") val discountPriceStr: String?, // 注意：第二个JSON中discountPriceStr可为null
    @SerializedName("needPreInput") val needPreInput: Boolean,
    @SerializedName("prefillCompleted") val prefillCompleted: Boolean,
    @SerializedName("ipId") val ipId: Any?, // JSON中为null，用Any?表示
    @SerializedName("ipCityId") val ipCityId: Int,
    @SerializedName("hasCouponAndIp") val hasCouponAndIp: Boolean,
    @SerializedName("purGroupId") val purGroupId: Any?, // JSON中为null，用Any?表示
    @SerializedName("purGoodsId") val purGoodsId: Any?, // JSON中为null，用Any?表示
    @SerializedName("buyInstructionType") val buyInstructionType: Int,
    @SerializedName("dyProjectId") val dyProjectId: Any?, // JSON中为null，用Any?表示
    @SerializedName("userAvatarList") val userAvatarList: Any?, // JSON中为null，用Any?表示
    @SerializedName("wantToSeeCount") val wantToSeeCount: Any?, // JSON中为null，用Any?表示
    @SerializedName("condRefund") val condRefund: Int,
    @SerializedName("couponActivityTagVO") val couponActivityTagVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("privilegeActivityStatus") val privilegeActivityStatus: Any?, // JSON中为null，用Any?表示
    @SerializedName("structureRefund") val structureRefund: Boolean,
    @SerializedName("needRealNameView") val needRealNameView: Any?, // JSON中为null，用Any?表示
    @SerializedName("isNewMode") val isNewMode: Int,
    @SerializedName("zonest") val zonest: Any?, // JSON中为null，用Any?表示
    @SerializedName("publicityStatus") val publicityStatus: Int,
    @SerializedName("straightLineDistance") val straightLineDistance: Any?, // JSON中为null，用Any?表示
    @SerializedName("externalActivity") val externalActivity: Boolean,
    @SerializedName("mchaoTag") val mchaoTag: Any?, // JSON中为null，用Any?表示
    @SerializedName("stockOut") val stockOut: Boolean
)

data class PerformanceLabelVO(
    @SerializedName("priceLabel") val priceLabel: Int,
    @SerializedName("contentLabel") val contentLabel: Int,
    @SerializedName("saleLabel") val saleLabel: Int,
    @SerializedName("billBoardLabel") val billBoardLabel: Any? // JSON中为null，用Any?表示
)

data class SaleRemindVO(
    @SerializedName("needRemind") val needRemind: Int,
    @SerializedName("onSaleTime") val onSaleTime: Long,
    @SerializedName("onSaleStatus") val onSaleStatus: Int,
    @SerializedName("needCountDown") val needCountDown: Boolean,
    @SerializedName("countdownTime") val countdownTime: Any?, // 第二个JSON中countdownTime可为null
    @SerializedName("register") val register: Any?, // JSON中为null，用Any?表示
    @SerializedName("phoneNumber") val phoneNumber: Any?, // JSON中为null，用Any?表示
    @SerializedName("needPreSelect") val needPreSelect: Any?, // JSON中为null，用Any?表示
    @SerializedName("needTicketNum") val needTicketNum: Any? // JSON中为null，用Any?表示
)

data class Promotional(
    @SerializedName("discountList") val discountList: List<DiscountList>?, // 第二个JSON中discountList可为null
    @SerializedName("setTicketList") val setTicketList: Any?, // JSON中为null，用Any?表示
    @SerializedName("preferenceList") val preferenceList: Any?, // JSON中为null，用Any?表示
    @SerializedName("projectGiftVO") val projectGiftVO: Any?, // JSON中为null，用Any?表示
    @SerializedName("supportWenhui") val supportWenhui: Boolean,
    @SerializedName("wenhuiActivityUrl") val wenhuiActivityUrl: Any?, // JSON中为null，用Any?表示
    @SerializedName("businessBatchList") val businessBatchList: List<Any>, // JSON中为空列表，用List<Any>表示
    @SerializedName("promotionalShowNum") val promotionalShowNum: Int,
    @SerializedName("hasUsableVoucher") val hasUsableVoucher: Boolean,
    @SerializedName("activityRuleList") val activityRuleList: Any?, // JSON中为null，用Any?表示
    @SerializedName("promotionDetails") val promotionDetails: List<PromotionDetails>
)

data class DiscountList(
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("ticketPrice") val ticketPrice: String,
    @SerializedName("sellPrice") val sellPrice: String,
    @SerializedName("discountRate") val discountRate: String,
    @SerializedName("discountShow") val discountShow: String
)

data class PromotionDetails(
    val type: Int,
    val details: List<Detail>
)

data class Detail(
    val text: String,
    val remain: Any?, // JSON中为null，用Any?表示
    @SerializedName("ticketId") val ticketId: Long?, // 第二个JSON中ticketId可为null
    @SerializedName("baseTicketId") val baseTicketId: Any?, // JSON中为null，用Any?表示
    @SerializedName("setNumber") val setNumber: Any? // JSON中为null，用Any?表示
)

data class ServiceTitle(
    val type: Int,
    val key: String,
    val value: String
)

data class ProjectReserveVO(
    @SerializedName("copywriter") val copywriter: Any?, // JSON中为null，用Any?表示
    val num: Any?, // JSON中为null，用Any?表示
    @SerializedName("isDisplay") val isDisplay: Boolean
)

data class AttrMaps(
    @SerializedName("serverTime") val serverTime: Long
)