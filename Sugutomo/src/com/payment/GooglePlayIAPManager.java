package com.payment;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.cls.sugutomo.BaseTabActivity;
import com.cls.sugutomo.api.InAppPurchaseAPI;
import com.cls.sugutomo.api.InAppPurchaseAPICallbackInterface;
import com.cls.sugutomo.apiclient.Params;
import com.cls.sugutomo.manager.SessionManager;
import com.cls.sugutomo.utils.Global;
import com.loopj.android.http.RequestParams;
import com.payment.util.Base64;
import com.payment.util.IabHelper;
import com.payment.util.IabHelper.OnIabSetupFinishedListener;
import com.payment.util.IabResult;
import com.payment.util.Inventory;
import com.payment.util.Purchase;
import com.payment.util.SimpleCrypto;


/**
 * 
 * @author codelovers call getIntance().onCreate() in oncreate activity handle
 *         buy item. call getIntance().onDestroy() in onDestroy activity handle
 *         buy item. call getIntance().onActivityResult() in onActivityResult
 *         activity handle
 * 
 */
public class GooglePlayIAPManager implements InAppPurchaseAPICallbackInterface {
	private static final String TAG = BaseTabActivity.class.getSimpleName();
	private static int REQUEST_BUY_ITEM = 1;
	IabHelper mHelper;
	private Activity mActivity;
	private PaymentListenerIAP paymentListener = null;
	public List<String> consumeItem, nonConsumeItem;
	public static GooglePlayIAPManager instance = null;
	private String base64EncodedPublicKey;
	private String payload;
	private SessionManager mSession;
	private boolean isBuying=false;
	private static int cost;
	public static GooglePlayIAPManager getIntance() {
		if (instance == null) {
			instance = new GooglePlayIAPManager();
		}
		return instance;

	}


	/**
	 * 
	 * @param ac
	 * @param listener
	 * @param consumeItem
	 *            : list item can buy multi-time
	 * @param nonConsumeItem
	 *            : list item can buy only one per account GooglePlay
	 * @param payload
	 *            : use userid
	 */
	public void onCreate(Activity ac, PaymentListenerIAP listener,
			String _payload, List<String> _consumeItem,
			List<String> _nonConsumeItem) {
		// TODO Auto-generated method stub
		isBuying=false;
		cost=0;
		mActivity = ac;
		paymentListener = listener;
		consumeItem = _consumeItem;
		nonConsumeItem = _nonConsumeItem;
		payload = _payload;
		//base64EncodedPublicKey =SimpleCrypto.encrypt(Global.apiSecretKey, ConfigIAP.base64EncodedPublicKey);
		//Log.v("keystore", "key encrypt:" + base64EncodedPublicKey);
		base64EncodedPublicKey = SimpleCrypto.decrypt(Global.apiSecretKey,ConfigIAP.base64EncodedPublicKey);
		//Log.v("keystore", "key decrypt:" + base64EncodedPublicKey);
		
		mSession = SessionManager
				.getInstance(mActivity.getApplicationContext());
		setupIaHelper(null);

	}

	private void setupIaHelper(OnIabSetupFinishedListener listener) {
		// Create the helper, passing it our context and the public key to
		// verify signatures with
		Log.d(TAG, "Creating IAB helper.");
		mHelper = new IabHelper(mActivity, base64EncodedPublicKey);
		Log.e("", "mHelper is create");
		// enable debug logging (for a production application, you should set
		// this to false).
		mHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		Log.d(TAG, "Starting setup.");
		if (listener != null) {
			mHelper.startSetup(listener);
		} else {
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					Log.d(TAG, "Setup finished.");

					if (!result.isSuccess()) {
						paymentListener.onSetupIAP(
								false,
								"Problem setting up in-app billing: "
										+ result.getMessage());
						return;
					}

					// Have we been disposed of in the meantime? If so, quit.
					if (mHelper == null)
						return;

					// IAB is fully set up. Now, let's get an inventory of stuff
					// we
					// own.
					Log.d(TAG, "Setup successful. Querying inventory");

					mHelper.queryInventoryAsync(false, mQueryFinishedListener);
				}
			});
		}
	}

	IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
			if (result.isFailure()) {
				// handle error
				alert("Problem gettting list item in-app billing"
						+ result.getMessage() + "\n. Press Ok to retry");

				return;
			}
			Log.d(TAG, "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the non-consume item like remove ads. So use it
			if (nonConsumeItem != null && nonConsumeItem.size() > 0) {
				int size = nonConsumeItem.size();
				for (int i = 0; i < size; i++) {
					Purchase purchase = inventory.getPurchase(nonConsumeItem
							.get(i));
					if (purchase != null) {
						Log.d(TAG, "We have forever item: remove ads,...");
						verifyDeveloperPayload(purchase, false, true);
					}
				}
			}
			// Do we have the consume item like buy point. But consumeAsync
			// return false previous time,
			// so this time use it again, after consumeAsync sucess it's not in
			// inventory again until new buy
			if (consumeItem != null && consumeItem.size() > 0) {
				int size = consumeItem.size();
				for (int i = 0; i < size; i++) {
					Purchase purchase = inventory.getPurchase(consumeItem
							.get(i));

					if (purchase != null) {
						verifyDeveloperPayload(purchase, false, false);
					}

				}
			}
		}
	};

	/**
	 * 
	 * @param itemID
	 *            id in google play store
	 * @param payload
	 *            string to unique purchase, can pass null
	 */
	public void buyItemPlayStore(final String itemID,int cost) {
		if(isBuying) {
			//complain("isbunging");
			return;
		}
		if (mHelper == null) {
			// case was remove for some reason.. setup again
			setupIaHelper(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					Log.d(TAG, "Setup agian finished.");
					if (!result.isSuccess()) {
						paymentListener.onSetupIAP(
								false,
								"Problem setting up in-app billing: "
										+ result.getMessage());
						return;
					}
					// Have we been disposed of in the meantime? If so, quit.
					if (mHelper == null)
						return;
					isBuying=true;
					mHelper.launchPurchaseFlow(mActivity, itemID,
							REQUEST_BUY_ITEM, mPurchaseFinishedListener,
							payload);
					
				}
			});
		} else {
			if (mHelper.mSetupDone) {
				GooglePlayIAPManager.cost=cost;
				isBuying=true;
				mHelper.launchPurchaseFlow(mActivity, itemID, REQUEST_BUY_ITEM,
						mPurchaseFinishedListener, payload);
				
			} else {
				complain("Google Play can't connect now. Wait a minute and retry");
			}
		}
	}

	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result,
				final Purchase purchase) {
			isBuying=false;
			Log.v("isBuying false", "isBuying false");
			if (result.isFailure()) {
				Log.v("google pay ment", "" + result);
				// complain("Error purchasing: " + result);
				return;
			}
			String itemID = purchase.getSku();
			if (consumeItem != null && consumeItem.contains(itemID)) {
				verifyDeveloperPayload(purchase, true, false);
			} else if (nonConsumeItem != null
					&& nonConsumeItem.contains(itemID)) {
				verifyDeveloperPayload(purchase, true, true);
			}

		}
	};

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
				+ data);
		if (mHelper == null)
			return false;
		// Pass on the activity result to the helper for handling
		return mHelper.handleActivityResult(requestCode, resultCode, data);
	}

	public void onDestroy() {
		if (mHelper != null)
			mHelper.dispose();
		mHelper = null;
		Log.e("", "mHelper is destroy");

	}

	/** Verifies the developer payload of a purchase. */
	void verifyDeveloperPayload(Purchase p, boolean isShowProgress,
			boolean isNonconsumeItem) {
		String payload = p.getDeveloperPayload();
		if (!payload.equalsIgnoreCase(this.payload)) {
			//Log.d(TAG, "payload " + this.payload + "not math with" + payload);
			return;
		}

		String signture = p.getSignature();
		String jsonPurchaseInfo = Base64.encode(p.getOriginalJson().getBytes());

		InAppPurchaseAPI api = new InAppPurchaseAPI(this, mActivity, p,
				isShowProgress, isNonconsumeItem);
		RequestParams request = new RequestParams();
		request.put(Params.PARAM_USER_ID_, mSession.getUserId());
		request.put(Params.PARAM_TOKEN_, mSession.getToken());
		request.put(Params.PARAM_PURCHASE_INFO, jsonPurchaseInfo);
		request.put(Params.PARAM_PURCHASE_SIGNTURE, signture);
		request.put(Params.PARAM_PURCHASE_MONEY_YEN, cost+"");

//		Log.v(TAG, "jsonPurchaseInfo: " + jsonPurchaseInfo);
//		Log.v(TAG, "signture: " + signture);
		api.setParams(request);
		api.onRunButtonPressed();

	}

	void complain(String message) {
		Log.e(TAG, "****  Error: " + message);
		alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(mActivity);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		Log.d(TAG, "Showing alert dialog: " + message);
		bld.create().show();
	}

	void alert(String message, DialogInterface.OnClickListener onclickListener) {
		AlertDialog.Builder bld = new AlertDialog.Builder(mActivity);
		bld.setMessage(message);
		bld.setPositiveButton("OK", onclickListener);
		bld.setNegativeButton("Cancel", null);
		Log.d(TAG, "Showing alert dialog: " + message);
		bld.create().show();
	}

	// Called when consumption is complete
	IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Log.d(TAG, "Consumption finished. Purchase: " + purchase
					+ ", result: " + result);
			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;
			// if (result.isSuccess()) {
			// paymentListener.onPurchaseItem(true, purchase.getSku());
			// } else {
			// paymentListener.onPurchaseItem(false, purchase.getSku());
			// }
			Log.d(TAG, "End consumption flow.");
		}
	};

	@Override
	public void handleReceiveData(boolean sucess, boolean isNonconsumeItem,
			String response, Purchase p) {
		// TODO Auto-generated method stub
		if (!sucess) {
			return;
		} else {

			paymentListener.onPurchaseItem(true, p.getSku());

		}
		if (mHelper != null && !isNonconsumeItem)
			mHelper.consumeAsync(p, mConsumeFinishedListener);
	}
}
