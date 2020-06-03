package com.mainback.wxfacepaydemo;

import okhttp3.MediaType;

/**
 * @author Loki_Zhou
 * @Date 2020/5/29
 **/
public class FacePayConfig {
    /*
     * API秘钥   t0e2df73dfgmi12nhjij32mvup5j12dw
     * 开发者ID(AppID)   wx24ba125f12d3e402   //appid
     * 银石支付   AppID：wx25c18b6e91b76812   //sub_appid
     * 服务商 商户号：1507438991 //mch_id
     * 子商户号：1588265101 //sub_mch_id
     */
    public static final String SecretKey = "t0e2df73dfgmi12nhjij32mvup5j12dw";//API秘钥
    public static final String APP_ID = "wx24ba125f12d3e402"; //商户号绑定的公众号/小程序 appid
    public static final String SUB_APP_ID = "wx25c18b6e91b76812";//子商户绑定的公众号/小程序 appid(服务商模式)
    public static final String MCH_ID = "1507438991";//商户号
    public static final String SUB_MCH_ID = "1588265101";//子商户号(服务商模式)
    public static final MediaType JsonType = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType XmlType = MediaType.parse("text/xml; charset=utf-8");


}
