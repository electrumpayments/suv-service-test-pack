package io.electrum.suv.handler.voucher;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class VoucherConfirmationHandler extends BaseHandler {
    public VoucherConfirmationHandler(HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    protected String getRequestName() {
        return "Voucher Confirmation";
    }
}
