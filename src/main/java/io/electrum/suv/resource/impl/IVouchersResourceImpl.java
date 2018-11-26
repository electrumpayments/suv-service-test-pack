package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IVouchersResource;
import io.electrum.suv.api.models.ProvisionRequest;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.TenderAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

public class IVouchersResourceImpl implements IVouchersResource {
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
