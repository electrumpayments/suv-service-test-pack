package io.electrum.suv.resource.impl;

import javax.ws.rs.Path;

import io.electrum.suv.api.IRefundsResource;
import io.electrum.suv.api.utils.Paths;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path(Paths.BASE_PATH)
@Api(description = "the Single Use Voucher API", authorizations = { @Authorization("httpBasic") })
public class RefundsResource extends io.electrum.suv.api.RefundsResource {
   private static RefundsResourceImpl instance = null;

   @Override
   protected IRefundsResource getResourceImplementation() {
      if (instance == null) {
         instance = new RefundsResourceImpl();
      }
      return instance;
   }

}
