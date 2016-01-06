package com.payment;


public interface PaymentListenerIAP {
	public void onSetupIAP(boolean isSucess,String message);
	public void onPurchaseItem(boolean isSucess,String itemId);
}
