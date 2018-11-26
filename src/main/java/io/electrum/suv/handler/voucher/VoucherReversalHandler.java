package io.electrum.suv.handler.voucher;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class VoucherReversalHandler extends BaseHandler {
    public VoucherReversalHandler(HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    protected String getRequestName() {
        return "Voucher Reversal";
    }
}
