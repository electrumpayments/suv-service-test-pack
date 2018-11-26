package io.electrum.suv.handler.refund;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class RefundVoucherHandler extends BaseHandler {
    public RefundVoucherHandler(HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    protected String getRequestName() {
        return "Refund Voucher";
    }
}
