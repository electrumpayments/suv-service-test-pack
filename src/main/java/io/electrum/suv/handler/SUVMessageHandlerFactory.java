package io.electrum.suv.handler;

import javax.ws.rs.core.HttpHeaders;

import io.electrum.suv.handler.redeem.RedeemConfirmationHandler;
import io.electrum.suv.handler.redeem.RedeemReversalHandler;
import io.electrum.suv.handler.redeem.RedeemVoucherHandler;
import io.electrum.suv.handler.refund.RefundConfirmationHandler;
import io.electrum.suv.handler.refund.RefundReversalHandler;
import io.electrum.suv.handler.refund.RefundVoucherHandler;
import io.electrum.suv.handler.voucher.VoucherConfirmationHandler;
import io.electrum.suv.handler.voucher.VoucherProvisionHandler;
import io.electrum.suv.handler.voucher.VoucherReversalHandler;

public class SUVMessageHandlerFactory {
   public static RedeemConfirmationHandler getRedeemConfirmationHandler(HttpHeaders httpHeaders) {
      return new RedeemConfirmationHandler(httpHeaders);
   }

   public static RedeemReversalHandler getRedeemReversalHandler(HttpHeaders httpHeaders) {
      return new RedeemReversalHandler(httpHeaders);
   }

   public static RedeemVoucherHandler getRedeemVoucherHandler(HttpHeaders httpHeaders) {
      return new RedeemVoucherHandler(httpHeaders);
   }

   public static RefundConfirmationHandler getRefundConfirmationHandler(HttpHeaders httpHeaders) {
      return new RefundConfirmationHandler(httpHeaders);
   }

   public static RefundReversalHandler getRefundReversalHandler(HttpHeaders httpHeaders) {
      return new RefundReversalHandler(httpHeaders);
   }

   public static RefundVoucherHandler getRefundVoucherHandler(HttpHeaders httpHeaders) {
      return new RefundVoucherHandler(httpHeaders);
   }

   public static VoucherConfirmationHandler getVoucherConfirmationHandler(HttpHeaders httpHeaders) {
      return new VoucherConfirmationHandler(httpHeaders);
   }

   public static VoucherProvisionHandler getVoucherProvisionHandler(HttpHeaders httpHeaders) {
      return new VoucherProvisionHandler(httpHeaders);
   }

   public static VoucherReversalHandler getVoucherReversalHandler(HttpHeaders httpHeaders) {
      return new VoucherReversalHandler(httpHeaders);
   }

   public static ResetHandler getResetHandler(HttpHeaders httpHeaders) {
      return new ResetHandler(httpHeaders);
   }
}
