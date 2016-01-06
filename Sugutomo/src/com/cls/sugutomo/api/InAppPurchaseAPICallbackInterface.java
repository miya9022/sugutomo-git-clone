package com.cls.sugutomo.api;

import com.payment.util.Purchase;

public interface InAppPurchaseAPICallbackInterface {
    public void  handleReceiveData(boolean sucess, boolean isNonconsumeItem, String response,Purchase p);
}
