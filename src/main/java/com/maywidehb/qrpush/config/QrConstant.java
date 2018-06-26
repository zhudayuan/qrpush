package com.maywidehb.qrpush.config;

public interface QrConstant {

    /**
     * 二维码
     * @author DaY
     *
     */
    interface TCODE{
        /** 微信小程序类型,优惠券小程序,优惠券详情使用  */
        public static final String WXCODE_HAPP = "00";
        /** 微信小程序类型,优惠券小程序,小程序主界面使用 */
        public static final String WXCODE_HAPP_INDEX = "10";
        /** 微信小程序类型,优惠券小程序,用户使用优惠券二维码 */
        public static final String WXCODE_USE_COUPON= "40";
        /** 微信小程序类型,优惠券小程序,跳转到广告页面    11~relation~qrid*/
        public static final String COUPON_AD= "11";

        /**  *微信二维码类型,世界杯 */
        public static final String WC= "WC";
        /**  *微信二维码类型,跳转到其他app */
        public static final String JUMP2APP= "JUMP2APP";
        /** 微信小程序类型,TV挂角续费二维码  20~serviceID~cardId*/
        public static final String WXCODE_TV_RENEWAL  = "20";
        /** 微信小程序类型,TV挂角订购二维码  21~serviceID~cardId*/
        public static final String WXCODE_TV_ORDER= "21";
//		/** 微信小程序类型,TV挂角宽带续费二维码 */
//		public static final String WXCODE_TV_CM= "22";
        /** 微信小程序类型,TV挂角基本包订购二维码 23~serviceID~cardId*/
        public static final String WXCODE_TV_BASEORDER= "23";
        /** 微信小程序类型,TV挂角基本包续费二维码 24~serviceID~cardId*/
        public static final String WXCODE_TV_BASERENEWAL= "24";

        /** 微信二维码,方形 TV 订购二维码  不带参数*/
        public static final String WXACODE_TV_F= "30";

        /** 微信小程序类型,投票小程序 ,店家添加店员 二维码 */
        public static final String WXCODE_SHOP_ASSIST= "50";
        /** 微信小程序类型，投票小程序 ,管理员生成投票项目二维码**/
        public static final String WXCODE_VOT = "60";
        /** 微信小程序类型，电视剧节目二维码**/
        public static final String WXCODE_NEWS = "70";
        /** 微信小程序类型，第三方跳转**/
        public static final String  THIRD_PARTY = "80";
    }

    interface PARAM {
        /** 广告位  1 - 中间,  2 - 右下角*/
        public static final String BI_AID_M = "81";
        public static final String BI_AID_R = "101";
        public static final String BI_AD_KEY = "2948C14F7ADC8896E0538D11CD0A3FF2";
        public static final String  QR_STATIC_HOST = CF.qr.staticHost;
        /** 默认二维码  M - 中间,  R - 右下角*/
        public static  String QR_RULE_M = "{\"bw\":650,\"bl\":650,\"backhp\":35,\"backwp\":315,\"backsize\":650,\"qrwp\":490,\"qrhp\":297,\"qrsize\":300,\"qrurl\":\"https://www.htrnpay.cn/wx/1/\",\"workhours\":60000,\"aftertime\":0,\"backurl\":\""+QR_STATIC_HOST+"/gd/6501.png\",\"countdown\":false}";
//        public static  String QR_RULE_R = "{\"bw\":185,\"bl\":185,\"backhp\":515,\"backwp\":1075,\"backsize\":185,\"qrwp\":1112,\"qrhp\":566,\"qrsize\":112,\"qrurl\":\"https://www.htrnpay.cn/wx/1/\",\"workhours\":60000,\"aftertime\":0,\"backurl\":\""+QR_STATIC_HOST+"/gd/185.png\",\"countdown\":false}";
        //新 250
        public static  String QR_RULE_R = "{\"bw\":250,\"bl\":250,\"backhp\":470,\"backwp\":1030,\"backsize\":250,\"qrwp\":1080,\"qrhp\":540,\"qrsize\":150,\"qrurl\":\"https://www.htrnpay.cn/wx/1/\",\"workhours\":60000,\"aftertime\":0,\"backurl\":\""+QR_STATIC_HOST+"/gd/250.png\",\"countdown\":false}";


    }


}