package com.ltz.aliwechatpaydemo.events;

/**
 * User: Tenz Liu
 * Date: 2017-11-06
 * Time: 11-53
 * Description: TODO
 */

public class Event {

    /**
     * 微信支付回调
     */
    public static class WXPayResultEvent {
        public final static int CODE_SUCCESS = 1;
        public final static int CODE_CANCEL = 2;
        public final static int CODE_FAILED = 3;
        public int code;

        public WXPayResultEvent(int errCode) {
            this.code = errCode;
        }
    }

}
