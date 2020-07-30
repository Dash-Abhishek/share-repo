package com.goldeneye.md.productsearch

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;
import com.google.gson.Gson
import com.goldeneye.md.objectdetection.DetectedObjectInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Makes a call to external search API to retrieve data  */
class SearchEngine(context: Context) {

    private val searchRequestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val requestCreationExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun search(
            detectedObject: DetectedObjectInfo,
            listener: (detectedObject: DetectedObjectInfo, productList: List<Product>) -> Unit
    ) {
        var productList:List<Product> = ArrayList<Product>()

        val httpAsync = "https://goldeneyesearch.herokuapp.com/products/search"
                .httpGet(listOf("query" to detectedObject.labels[0].text))
                .responseString { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            println(ex)
                        }
                        is Result.Success -> {
                            val data = result.get()
                            val gson = Gson()
                            productList = gson.fromJson(data, Array<Product>::class.java).toList()
                        }
                    }
                }

        httpAsync.join()

        listener.invoke(detectedObject, productList)

    }

    fun shutdown() {
        searchRequestQueue.cancelAll(TAG)
        requestCreationExecutor.shutdown()
    }

    companion object {
        private const val TAG = "SearchEngine"
    }
}
