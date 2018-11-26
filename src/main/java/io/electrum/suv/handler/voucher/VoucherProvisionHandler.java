package io.electrum.suv.handler.voucher;

import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;

public class VoucherProvisionHandler extends BaseHandler {
    public VoucherProvisionHandler(HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    protected String getRequestName() {
        return "Voucher Provision";
    }
}
