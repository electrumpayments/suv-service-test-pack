package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IRedemptionsResource;
import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.handler.SUVMessageHandlerFactory;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

//TODO Implement methods correctly
public class RedemptionsResourceInterfaceImpl implements IRedemptionsResource {
   private static final Logger log = LoggerFactory.getLogger(RedemptionsResourceInterfaceImpl.class);

   @Override
   public void redeemVoucher(
         RedemptionRequest body,
         SecurityContext securityContext, //unused
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
              SUVMessageHandlerFactory.getRedeemVoucherHandler(httpHeaders)
                      .handle(/* requestId, confirmationId, */ body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);


   }

   @Override
   public void reverseRedemption(
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
              SUVMessageHandlerFactory.getRedeemReversalHandler(httpHeaders)
                      .handle(/* requestId, confirmationId, */ body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void confirmRedemption(
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
              SUVMessageHandlerFactory.getRedeemConfirmationHandler(httpHeaders)
                      .handle(/* requestId, confirmationId, */ body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }
}
