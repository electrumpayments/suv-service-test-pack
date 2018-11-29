package io.electrum.suv.handler.redeem;

import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.handler.BaseHandler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class RedeemVoucherHandler extends BaseHandler {
    public RedeemVoucherHandler(HttpHeaders httpHeaders) {
        super(httpHeaders);
    }

    @Override
    protected String getRequestName() {
        return "Redeem Voucher";
    }

    public Response handle(/*String requestId, String confirmationID, */ RedemptionRequest redemptionRequest, UriInfo uriInfo) {
        try{
            Response rsp = null;

            //TODO Implement this correctly

            return rsp;
        }catch (Exception e){
            return logAndBuildException(e);
        }

    }
}
