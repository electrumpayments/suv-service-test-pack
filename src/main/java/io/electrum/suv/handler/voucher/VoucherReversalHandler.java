package io.electrum.suv.handler.voucher;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.handler.BaseHandler;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.FormatException;
import io.electrum.suv.server.model.ValidationResponse;
import io.electrum.suv.server.util.RequestKey;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class VoucherReversalHandler extends BaseHandler {

   // --Commented out by Inspection (2018/12/07, 07:29):private static final Logger log =
   // LoggerFactory.getLogger(VoucherProvisionHandler.class);

   public VoucherReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(BasicReversal reversal) {
      try {
         ValidationResponse validationRsp = new ValidationResponse(null);

         // The UUID of this request
         String reversalUuid = reversal.getId();
         // The UUID identifying the request that this reversal relates to
         String voucherId = reversal.getRequestId();

         VoucherModelUtils.validateUuid(reversalUuid);
         VoucherModelUtils.validateUuid(voucherId);
         VoucherModelUtils.validateThirdPartyIdTransactionIds(reversal.getThirdPartyIdentifiers());

         // TODO check this in airtime
         validationRsp = VoucherModelUtils.canReverseVoucher(voucherId, reversalUuid, username, password);
         if (validationRsp.hasErrorResponse()) {
            if (validationRsp.getResponse().getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addVoucherReversalToCache(reversal);
            }
            return validationRsp.getResponse();
         }

         addVoucherReversalToCache(reversal);

         validationRsp.setResponse(Response.accepted((reversal)).build()); // TODO Ask Casey if this is ok

         return validationRsp.getResponse();

      } catch (FormatException fe) {
         throw fe;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addVoucherReversalToCache(BasicReversal basicReversal) {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            SUVTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, basicReversal.getRequestId());
      reversalRecords.put(reversalKey, basicReversal);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Reversal";
   }
}
