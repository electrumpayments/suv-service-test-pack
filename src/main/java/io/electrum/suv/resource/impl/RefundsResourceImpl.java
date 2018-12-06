package io.electrum.suv.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.api.IRefundsResource;
import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.handler.SUVMessageHandlerFactory;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;

//TODO Implement methods correctly
public class RefundsResourceImpl implements IRefundsResource {
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   @Override
   public void confirmRefund(
         BasicAdvice body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            SUVMessageHandlerFactory.getRefundConfirmationHandler(httpHeaders)
                  .handle(/* requestId, confirmationId, */ body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

   }

   @Override
   public void reverseRefund(
         BasicReversal body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            SUVMessageHandlerFactory.getRefundReversalHandler(httpHeaders)
                  .handle(/* requestId, confirmationId, */ body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void refundVoucher(
         RefundRequest body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            SUVMessageHandlerFactory.getRefundVoucherHandler(httpHeaders)
                  .handle(/* requestId, confirmationId, */ body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }
}
