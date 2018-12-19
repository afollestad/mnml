/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package com.afollestad.mnmlscreenrecord.donate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.BillingResponse.BILLING_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponse.DEVELOPER_ERROR
import com.android.billingclient.api.BillingClient.BillingResponse.ERROR
import com.android.billingclient.api.BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED
import com.android.billingclient.api.BillingClient.BillingResponse.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponse.ITEM_NOT_OWNED
import com.android.billingclient.api.BillingClient.BillingResponse.ITEM_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponse.OK
import com.android.billingclient.api.BillingClient.BillingResponse.SERVICE_DISCONNECTED
import com.android.billingclient.api.BillingClient.BillingResponse.SERVICE_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponse.USER_CANCELED
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.bugsnag.android.Bugsnag
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber.d as log

/**
 * Abstracts billing API usage for the app's donation functionality.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface DonateClient : LifecycleObserver {

  /** Emits SKUs of purchased items. */
  fun onPurchase(): Observable<String>

  /** Emits a set of SKY details when and if the client is ready to purchase. */
  fun onReady(): Single<List<SkuDetails>>

  /** Emits when an error that should be shown to the user occurs. */
  fun onError(): Observable<Exception>

  /** Performs a purchase with a SKU. Returns true if the response code is OK. */
  fun makePurchase(
    activity: Activity,
    skuDetails: SkuDetails
  ): Boolean
}

/** @author Aidan Follestad (@afollestad) */
class RealDonateClient(
  private val app: Application
) : DonateClient, PurchasesUpdatedListener, LifecycleObserver {

  private val onPurchase = PublishSubject.create<String>()
  private val onIsReady = BehaviorSubject.create<List<SkuDetails>>()
  private val onError = BehaviorSubject.create<Exception>()
  private var client: BillingClient? = null

  @OnLifecycleEvent(ON_DESTROY)
  fun onDestroy() {
    log("onDestroy()")
    client?.endConnection()
  }

  override fun onPurchase(): Observable<String> = onPurchase

  override fun onReady(): Single<List<SkuDetails>> {
    ensureClient()
    return onIsReady.filter { it.isNotEmpty() }
        .take(1)
        .singleOrError()
  }

  override fun onError(): Observable<Exception> = onError

  override fun makePurchase(
    activity: Activity,
    skuDetails: SkuDetails
  ): Boolean {
    log("makePurchase(${skuDetails.sku})")
    val client = client ?: throw IllegalStateException("Client not initialized.")

    val flowParams = BillingFlowParams.newBuilder()
        .setSkuDetails(skuDetails)
        .build()
    val responseCode = client.launchBillingFlow(activity, flowParams)

    if (responseCode != OK) {
      onError.onNext(
          Exception(
              "Unable to donate, error code ${responseCode.billingCodeName()}. " +
                  "Please try again later :)"
          )
      )
      Bugsnag.notify(
          DonationException("makePurchase(${skuDetails.sku})", responseCode)
      )
    }
    return responseCode == BillingResponse.OK
  }

  @SuppressLint("SwitchIntDef")
  override fun onPurchasesUpdated(
    responseCode: Int,
    purchases: MutableList<Purchase>?
  ) {
    when (responseCode) {
      BillingResponse.USER_CANCELED -> log("onPurchasesUpdated() - User cancelled")
      BillingResponse.OK -> purchases?.forEach {
        log("onPurchasesUpdated() - Success for SKU ${it.sku}")
        onPurchase.onNext(it.sku)
      }
      else -> {
        onError.onNext(
            Exception(
                "Unable to donate, error code ${responseCode.billingCodeName()}. " +
                    "Please try again later :)"
            )
        )
        Bugsnag.notify(DonationException("onPurchasesUpdated()", responseCode))
      }
    }
  }

  private fun retrieveSkuDetails() {
    val skuList = app.resources.getStringArray(R.array.donation_skus)
        .toList()
    log("Retrieving SKU details for $skuList")

    val client = client ?: throw IllegalStateException("Client not initialized.")
    val params = SkuDetailsParams.newBuilder()
        .apply {
          setSkusList(skuList)
          setType(SkuType.INAPP)
        }
        .build()

    client.querySkuDetailsAsync(params) { code, detailsList ->
      log("Got SKU details result. Code = $code, list = $detailsList")
      onIsReady.onNext(detailsList.sortedBy { it.priceAmountMicros })
    }
  }

  private fun ensureClient() {
    log("ensureClient()")
    if (client == null) {
      log("ensureClient() - Client constructed")
      client = BillingClient.newBuilder(app)
          .setListener(this)
          .build()
    }

    val tempClient = client!!
    if (!tempClient.isReady) {
      log("ensureClient() - Client connecting...")
      tempClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(responseCode: Int) {
          if (responseCode == BillingResponse.OK) {
            log("Billing client setup finished successfully!")
            retrieveSkuDetails()
          } else {
            Bugsnag.notify(DonationException("client.startConnection()", responseCode))
            onError.onNext(
                Exception(
                    "Unable to donate, client setup error code ${responseCode.billingCodeName()}. " +
                        "Please try again later :)"
                )
            )
          }
        }

        override fun onBillingServiceDisconnected() {
          log("Billing client disconnected from service")
          onIsReady.onNext(emptyList())
        }
      })
    }
  }
}

internal class DonationException(
  action: String,
  responseCode: Int
) : Exception("$action unsuccessful - code = ${responseCode.billingCodeName()}")

private fun Int.billingCodeName(): String = when (this) {
  FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
  SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
  OK -> "OK"
  USER_CANCELED -> "USER_CANCELED"
  SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
  BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
  ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
  DEVELOPER_ERROR -> "DEVELOPER_ERROR"
  ERROR -> "ERROR"
  ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
  ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
  else -> "??"
}
