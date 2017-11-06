package com.ltz.aliwechatpaydemo.wxapi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.ltz.aliwechatpaydemo.R;
import com.ltz.aliwechatpaydemo.app.Constants;
import com.ltz.aliwechatpaydemo.events.Event;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import de.greenrobot.event.EventBus;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_result);

		api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			switch (resp.errCode) {
				case BaseResp.ErrCode.ERR_OK://支付成功
					EventBus.getDefault().post(new Event.WXPayResultEvent(Event.WXPayResultEvent.CODE_SUCCESS));
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
					EventBus.getDefault().post(new Event.WXPayResultEvent(Event.WXPayResultEvent.CODE_CANCEL));
					break;
				case BaseResp.ErrCode.ERR_SENT_FAILED://支付失败
				case BaseResp.ErrCode.ERR_COMM:
				case BaseResp.ErrCode.ERR_AUTH_DENIED:
				case BaseResp.ErrCode.ERR_UNSUPPORT:
					EventBus.getDefault().post(new Event.WXPayResultEvent(Event.WXPayResultEvent.CODE_FAILED));
					break;
			}
			finish();
		}
	}
}