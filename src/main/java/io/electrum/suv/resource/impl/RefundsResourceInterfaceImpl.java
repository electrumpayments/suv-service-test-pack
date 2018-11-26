package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IRefundsResource;
import io.electrum.suv.api.models.RefundRequest;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

//TODO Implement methods correctly
public class RefundsResourceInterfaceImpl implements IRefundsResource {
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

   }
}
