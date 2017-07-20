package com.cypherpunk.privacy.ui.account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserDataResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class UpgradePlanActivity extends BillingActivity implements PurchasingListener {

    // TODO
    private static final String SKU_MONTHLY = "monthly1295";
    private static final String SKU_ANNUALLY = "annually9995";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PurchasingService.registerListener(getApplicationContext(), this);

        // 現在ログオンしているユーザーのアプリ固有のIDとマーケットプレイスを取得する。
        // たとえば、ユーザーがアカウントを切り替えたり、複数のユーザーが同じ端末上でアプリにアクセスしている場合は、
        // このメソッドを呼び出して、現在のユーザーアカウントのレシートを確実に取得することができる。
        //
        // see PurchasingListener#onUserDataResponse
        final RequestId requestId = PurchasingService.getUserData();
        Timber.d("init: PurchasingService.getUserData : requestId = " + requestId);

        // 以前の課金や複数の端末から取り消された課金を取得する。
        // このメソッドから返されるPurchaseUpdatesResponseデータを保持しておき、
        // 更新する場合のみシステムに問い合わせることが推奨されている。
        // レスポンスはページ分割される。
        final RequestId requestId2 = PurchasingService.getPurchaseUpdates(false);
        Timber.d("init: PurchasingService.getPurchaseUpdates : requestId2 = " + requestId2);

        query();
    }

    private void query() {
        final Set<String> sku = new HashSet<>();
        sku.add(SKU_MONTHLY);
        sku.add(SKU_ANNUALLY);

        // アプリに表示するSKUセットのアイテムデータを取得する
        final RequestId requestId = PurchasingService.getProductData(sku);
        Timber.d("query : requestId:" + requestId);
    }

    @Override
    protected void purchase(@NonNull PurchaseItem.Type type) {
        final String sku;
        switch (type) {
            case MONTHLY:
                sku = SKU_MONTHLY;
                break;
            case ANNUALLY:
                sku = SKU_ANNUALLY;
                break;
            default:
                throw new IllegalStateException();
        }
        final RequestId requestId = PurchasingService.purchase(sku);
        Timber.d("purchase : requestId:" + requestId);
    }

    /**
     * {@link PurchasingService#getUserData()} を呼び出したあとに呼び出される。
     * 現在ログオンしているユーザーのUserIdとmarketplaceを判断します
     *
     * @param response UserDataResponse
     */
    @Override
    public void onUserDataResponse(UserDataResponse response) {
        Timber.d("onUserDataResponse: " + response);
    }

    /**
     * {@link PurchasingService#getPurchaseUpdates(boolean)} を呼び出したあとに呼び出される。
     * 購入履歴を取得します。このメソッドから返されるPurchaseUpdatesResponseデータを保持しておき、
     * 更新する場合のみシステムに問い合わせることが推奨されている。
     *
     * @param response PurchaseUpdatesResponse
     */
    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse response) {
        Timber.d("onPurchaseUpdatesResponse: " + response);
    }

    /**
     * {@link PurchasingService#getProductData(Set)} を呼び出したあとに呼び出される。
     * アプリから販売したいSKUに関する情報を取得する。
     * {@link PurchasingService#purchase(String)} で有効なSKUを使用する。
     *
     * @param response ProductDataResponse
     */
    @Override
    public void onProductDataResponse(ProductDataResponse response) {
        Timber.d("onProductDataResponse: " + response);
        final List<PurchaseItem> productItems = new ArrayList<>();

        final Map<String, Product> productData = response.getProductData();

        PurchaseItem monthlyItem = null;
        PurchaseItem annuallyItem = null;

        if (productData != null) {
            for (Product product : productData.values()) {
                switch (product.getSku()) {
                    case SKU_MONTHLY:
                        monthlyItem = new PurchaseItem(PurchaseItem.Type.MONTHLY,
                                product.getTitle(),
                                product.getDescription(),
                                product.getPrice(),
                                0);
                        break;
                    case SKU_ANNUALLY:
                        annuallyItem = new PurchaseItem(PurchaseItem.Type.ANNUALLY,
                                product.getTitle(),
                                product.getDescription(),
                                product.getPrice(),
                                0);
                        break;
                }
            }
        }

        if (monthlyItem == null || annuallyItem == null) {
            Toast.makeText(UpgradePlanActivity.this, "Purchase items not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        onQueryResult(monthlyItem, annuallyItem);
    }

    /**
     * {@link PurchasingService#purchase(String)} を呼び出したあとに呼び出される。
     * 購入のステータスを判断するために使用する。
     *
     * @param response PurchaseResponse
     */
    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        Timber.d("onPurchaseResponse: " + response);

        final PurchaseResponse.RequestStatus status = response.getRequestStatus();
        final Receipt receipt = response.getReceipt();
        switch (status) {
            case SUCCESSFUL:
                onPurchaseResult(new PurchaseResult(
                        true,
                        receipt == null ? null : receipt.getSku(),
                        receipt == null ? null : receipt.toJSON().toString(),
                        "purchased"));
                break;
            case ALREADY_PURCHASED:
                onPurchaseResult(new PurchaseResult(
                        false,
                        receipt == null ? null : receipt.getSku(),
                        receipt == null ? null : receipt.toJSON().toString(),
                        "already purchased"));
                break;
            case INVALID_SKU:
                onPurchaseResult(new PurchaseResult(
                        false,
                        receipt == null ? null : receipt.getSku(),
                        receipt == null ? null : receipt.toJSON().toString(),
                        "invalid SKU"));
                break;
            case FAILED:
                onPurchaseResult(new PurchaseResult(
                        false,
                        receipt == null ? null : receipt.getSku(),
                        receipt == null ? null : receipt.toJSON().toString(),
                        "failed"));
                break;
            case NOT_SUPPORTED:
                onPurchaseResult(new PurchaseResult(
                        false,
                        receipt == null ? null : receipt.getSku(),
                        receipt == null ? null : receipt.toJSON().toString(),
                        "not supported"));
                break;
            default:
                onPurchaseResult(new PurchaseResult(
                        false,
                        receipt == null ? null : receipt.getSku(),
                        receipt == null ? null : receipt.toJSON().toString(),
                        "unknown"));
                break;
        }
    }
}
