package io.electrum.suv.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.electrum.suv.api.IRedemptionsResource;
import io.electrum.suv.api.RedemptionsResource;
import io.electrum.suv.api.models.RedemptionRequest;
import io.electrum.suv.api.utils.Paths;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

//TODO Are PATHS necessary
@Path(Paths.BASE_PATH + Paths.REDEEM_VOUCHER)
@Api(description = "the Single Use Voucher API", authorizations = { @Authorization("httpBasic") })
public class RedemptionsResourceImpl extends RedemptionsResource {
   static IRedemptionsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   @Override
   protected IRedemptionsResource getResourceImplementation() {
      if (instance == null) {
         instance = new IRedemptionsResourceImpl();
      }
      return instance;
   }

}
