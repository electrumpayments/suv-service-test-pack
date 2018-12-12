package io.electrum.suv.handler.voucher;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.api.models.ProvisionResponse;
import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.resource.impl.SUVTestServer.VoucherState;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.TenderAdvice;

public class VoucherConfirmationHandler extends BaseHandler {
   /** The UUID identifying the request that this confirmation relates to */
   private String voucherProvisionUuid;

   public VoucherConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   private TenderAdvice confirmation;

   /**
    * Handle the response to a confirmVoucher request.
    *
    * See <a href=
    * "https://electrumpayments.github.io/suv-service-interface-docs/specification/operations/#confirmVoucher">SUV
    * Interface docs</a> for details.
    *
    * @param confirmation
    *           from request body
    * @return a {@link ProvisionResponse} for this transaction or a 400 Error if there is a format error or the voucher
    *         is already redeemed or reversed.
    */
   public Response handle(TenderAdvice confirmation) {
      try {
         Response rsp;

         // The UUID of this request
         String confirmationUuid = confirmation.getId();
         voucherProvisionUuid = confirmation.getRequestId();

         VoucherModelUtils.validateUuid(confirmationUuid);
         VoucherModelUtils.validateUuid(voucherProvisionUuid);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(confirmation.getThirdPartyIdentifiers());

         rsp = VoucherModelUtils.canConfirmVoucher(voucherProvisionUuid, confirmationUuid, username, password);
         if (rsp != null) {
            return rsp;
         }

         addVoucherConfirmationToCache(confirmation);

         rsp = Response.accepted((confirmation)).build(); // TODO Ask Casey if this is ok

         return rsp;
      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   /**
    * Adds the voucher confirmation request to the cache and stores an entry to the voucher in the list of existing
    * vouchers
    */
   private void addVoucherConfirmationToCache(TenderAdvice confirmation) {
      ConcurrentHashMap<RequestKey, TenderAdvice> confirmationRecords =
            SUVTestServerRunner.getTestServer().getVoucherConfirmationRecords();
      ConcurrentHashMap<String, VoucherState> confirmedExistingVouchers =
            SUVTestServerRunner.getTestServer().getConfirmedExistingVouchers();

      ProvisionResponse provisionRsp =
            SUVTestServerRunner.getTestServer()
                  .getVoucherResponseRecords()
                  .get(new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherProvisionUuid));

      String voucherCode = provisionRsp.getVoucher().getCode();

      RequestKey confirmationsKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherProvisionUuid);

      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);
      confirmedExistingVouchers.put(voucherCode, VoucherState.CONFIRMED_PROVISIONED);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Confirmation";
   }
}
