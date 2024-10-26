package ir.androidcoder.bazaarpayment

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import ir.androidcoder.bazaarpayment.payment.util.IabHelper
import ir.androidcoder.bazaarpayment.payment.util.Purchase
import ir.androidcoder.bazaarpayment.ui.theme.BazaarPaymentTheme
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /**
     * پرداخت درون برنامه ای بازار
     * پرداخت درون برنامه ای دو روش دارد...
     * 1- به روش قدیمی و با استفاده از کلاس ههای کمکی که به صورت دستی داخل پروژه قرار میگیرد...
     * 2- روش جدید با استفاده ز کتابخانه پولکی
     */

    //----------------------------------------------------------------------------------------------

    //--------------------------------------------------------------------------توضیحات روش قدیمی---
    /**
     * 1- پوشه payment را در پروژه خود کپی کنید
     * 2- پرمیشن کافه بازار را به manifest پروژه خود اضافه کنید
     * 3- با توجه نیاز توابع زیر را که با کانت روش قدیمی مخشص شده اند در پروژه حود در فایل فرگمنت یا اکتیویتی اضافه کنید
     */
    // روش قدیمی
    private lateinit var mHelper: IabHelper
    private var isPremium by mutableStateOf(false)
    private val rcRequest = 1372
    //----------------------------------------------------------------------------------------------

    //ایدی محصول در کافه بازار
    private var skuPremium = ""

    //---------------------------------------------------------------------------توضیحات روش جدید---
    /**
     * 1- ابتدا sdk پولکی را به گردل پروژه اضافه میکنیم . فراموش نشود که jitpack.io به پروژه اضافه شود
     * 2- تابعی که بر روی آن کامنت روش جدید گذاشته شده است استفاده شود
     */
    // روش جدید
    private lateinit var paymentConnection: Connection

    /*
    تذکر
    این پروژه و مثال به شما کمک میکند تا به راحتی درگاه درون برنامه ای بازار را راه اندازی کنید
    اما برای دسترسی به مراد و توابع بیشتر و شخصی سازی های بهتر میتوانید به مستندات رسمی بازار
    مراجعه کنید.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BazaarPaymentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    //روش قدیمی
                    setupIABHelper()
                }
            }
        }
    }

    //----------------------------------------------------------------------------------روش قدیمی---
    private fun setupIABHelper() {

        val base64EncodedPublicKey = "کلید بازار"

        mHelper = IabHelper(this, base64EncodedPublicKey)
        mHelper.startSetup { result ->
            if (!result.isSuccess) {
                Log.d("Ekavir", "Problem setting up In-app Billing: $result")
                return@startSetup
            }
            mHelper.queryInventoryAsync { result, inventory ->
                if (result.isFailure) {
                    Log.d("Ekavir", "Failed to query inventory: $result")
                } else {
                    isPremium = inventory.hasPurchase(skuPremium)
                    if (isPremium) {
                        consumePurchase(inventory.getPurchase(skuPremium))
                    }
                    startPurchaseFlow()
                }
            }
        }
    }

    private fun startPurchaseFlow() {
        mHelper.launchPurchaseFlow(this, skuPremium, rcRequest) { result, purchase ->
            if (result.isFailure) {
                Log.d("Ekavir", "Error purchasing: $result")
                mHelper.dispose()
                return@launchPurchaseFlow
            }
            if (purchase.sku == skuPremium) {
                isPremium = true
                sendToServer(purchase)
            }else{
                mHelper.dispose()
            }
        }
    }

    private fun sendToServer(purchase: Purchase) {


    }

    private fun consumePurchase(purchase: Purchase) {
        mHelper.consumeAsync(purchase) { _, result ->
            if (result.isSuccess) {
                isPremium = false
            }else{
                mHelper.dispose()
            }
        }
    }

    /*override fun onDestroy() {
        super.onDestroy()
        if (::mHelper.isInitialized) mHelper.dispose()
    }*/

    //----------------------------------------------------------------------------------------------

    //-----------------------------------------------------------------روش جدید با کتابخانه پولکی---
    private fun bazaar() {

        val localSecurityCheck = SecurityCheck.Enable(
            rsaPublicKey = "کلید بازار"
        )

        val paymentConfiguration = PaymentConfiguration(
            localSecurityCheck = localSecurityCheck
        )

        val payment = Payment(context = this, config = paymentConfiguration)

        val purchaseRequest = PurchaseRequest(
            productId = skuPremium,
            payload = "PAYLOAD"
        )

        paymentConnection = payment.connect {

            connectionSucceed {

                Toast.makeText(this@MainActivity, "اتصال به بازار قرار شد", Toast.LENGTH_SHORT).show()

                payment.purchaseProduct(
                    registry = activityResultRegistry,
                    request = purchaseRequest
                ) {
                    purchaseFlowBegan {
                        Toast.makeText(
                            this@MainActivity,
                            "هدایت کاربر به صفحه خرید بازار به درستی انجام شد",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    failedToBeginFlow { throwable ->
                        Toast.makeText(
                            this@MainActivity,
                            "هدایت کاربر به صفحه خرید بازار به درستی انجام نشد",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.v("bazaarTest", throwable.toString())
                    }
                    purchaseSucceed { purchaseEntity ->



                    }
                    purchaseCanceled {
                        Toast.makeText(this@MainActivity, "کاربر از خرید منصرف شد", Toast.LENGTH_SHORT)
                            .show()
                    }
                    purchaseFailed { throwable ->
                        Toast.makeText(this@MainActivity, "اطلاعات خرید یافت نشد", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }

            connectionFailed { throwable ->
                Toast.makeText(this@MainActivity, "اتصال به بازار قرار نشد", Toast.LENGTH_SHORT)
                    .show()
            }

            disconnected {
                Toast.makeText(this@MainActivity, "اتصال به بازار قطع شد", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        payment.consumeProduct("PURCHASE_TOKEN") {
            consumeSucceed {

            }
            consumeFailed { throwable ->

            }
        }


    }

    /*override fun onDestroy() {
        paymentConnection.disconnect()
        super.onDestroy()
    }*/

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BazaarPaymentTheme {
        Greeting("Android")
    }
}