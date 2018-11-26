package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IRedemptionsResource;
import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

//TODO Implement methods correctly
public class IRedemptionsResourceImpl implements IRedemptionsResource {
   @Override
   public void redeemVoucher(
         RedemptionRequest body,
         SecurityContext securityContext,
         Request request,
         AsyncResponse asyncResponse,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

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

   }
}
