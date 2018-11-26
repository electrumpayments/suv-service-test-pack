package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IVouchersResource;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.suv.handler.SUVMessageHandlerFactory;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

public class IVouchersResourceImpl implements IVouchersResource {
   private static final Logger log = LoggerFactory.getLogger(IVouchersResource.class);

   @Override
   public void confirmVoucher(
         TenderAdvice body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

   }

   @Override
   public void provisionVoucher(
         ProvisionRequest body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            SUVMessageHandlerFactory.getVoucherProvisionHandler(httpHeaders)
                  .handle(/* requestId, confirmationId, */ body);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void reverseVoucher(
         BasicReversal body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

   }
}
