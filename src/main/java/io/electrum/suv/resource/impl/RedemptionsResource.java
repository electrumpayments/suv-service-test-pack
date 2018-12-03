package io.electrum.suv.resource.impl;

import io.electrum.suv.api.IRedemptionsResource;
import io.electrum.suv.api.utils.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

import javax.ws.rs.Path;

@Path(Paths.BASE_PATH)
@Api(description = "the Single Use Voucher API", authorizations = { @Authorization("httpBasic") })
public class RedemptionsResource extends io.electrum.suv.api.RedemptionsResource {
   static RedemptionsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(RedemptionsResource.class);

   @Override
   protected IRedemptionsResource getResourceImplementation() {
      if (instance == null) {
         instance = new RedemptionsResourceImpl();
      }
      return instance;
   }

}
