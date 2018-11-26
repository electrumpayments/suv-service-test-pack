package io.electrum.suv.handler.redeem;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class RedeemVoucherHandler extends BaseHandler {
    public RedeemVoucherHandler(HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    protected String getRequestName() {
        return "Redeem Voucher";
    }
}
