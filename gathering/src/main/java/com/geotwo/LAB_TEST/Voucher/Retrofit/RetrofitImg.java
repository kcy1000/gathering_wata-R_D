package com.geotwo.LAB_TEST.Voucher.Retrofit;

import com.geotwo.LAB_TEST.Gathering.Retrofit.RetrofitExService;
import com.geotwo.LAB_TEST.Gathering.util.Constance;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitImg {

    private static RetrofitImg ourInstance = new RetrofitImg();
    public static RetrofitImg getInstance() {
        return ourInstance;
    }
    private RetrofitImg() {
    }

    OkHttpClient defaultHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
//                    .addHeader("Content-Type","image/jpeg")
                    .addHeader("Content-Type","temp/json")
                    .build();
            return chain.proceed(request);

        }
    }).build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constance.DEVE_JUDI_SERVER )
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .client(defaultHttpClient)
            .build();

    RetrofitExService service = retrofit.create(RetrofitExService.class);

    public RetrofitExService getService() {
        return service;
    }



}