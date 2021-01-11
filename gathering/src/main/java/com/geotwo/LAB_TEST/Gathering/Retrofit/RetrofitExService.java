package com.geotwo.LAB_TEST.Gathering.Retrofit;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitExService {

    @GET("/indoormap")
    Call<List<Data>> mapGpsSetting(@Query("coordx") String coordx,
                              @Query("coordy") String coordy);

    @GET("/subwayArea") // 지역정보
    Call<ResponseBody> subwayArea();

    @GET("/subwayLine") // 노선정보
    Call<ResponseBody> subwayLine(@Query("areaNo") String areaNo);

    @GET("/subwayStation")    //지하철정보
    Call<ResponseBody> subwayStation(@Query("areaNo") String areaNo,
                                     @Query("lineNo") String lineNo);

    @GET("/subwayGuideFloors") // 층수정보
    Call<ResponseBody> subwayGuideFloors(@Query("areaNo") String areaNo,
                                         @Query("lineNo") String lineNo,
                                     @Query("stationNo") String stationNo);


    @GET("/lidarSubwayGuideFloors") // 신규 층수정보
    Call<ResponseBody> lidarSubwayGuideFloors(@Query("areaNo") String areaNo,
                                         @Query("lineNo") String lineNo,
                                         @Query("stationNo") String stationNo);



    @GET("/subwayGuideLines")
    Call<ResponseBody> subwayGuideLines(@Query("areaNo") String areaNo,
                                         @Query("lineNo") String lineNo,
                                         @Query("stationnm") String stationnm,
                                        @Query("floor") String floor);


    @GET("/officeArea") // 기업위치정보
    Call<ResponseBody> officeArea();

    @GET("/officeInfo")    //기업이름정보
    Call<ResponseBody> officeInfo(@Query("areaNo") String areaNo);

    @GET("/officeFloor")    //기업층수 정보
    Call<ResponseBody> officeFloor(@Query("areaNo") String areaNo,
                                     @Query("companyNo") String lineNo);

    @GET("/categoryInfos") // poi 카테코리
    Call<ResponseBody> categoryInfos();

//    subwayGuideLines2

    @GET("/subwayGuideLines2")
    Call<ResponseBody> testGuideLine();


    @GET("/api/collect/pre-upload")
    Call<ResponseBody> pre_upload(@Query("gid") String gid,
                                  @Query("name") String name);

    @GET("/api/collect/post-upload/{uuid}")
    Call<ResponseBody> post_upload(@Path("uuid") String uuid);


    @Multipart
    @POST("/api/collect")
    Call<ResponseBody> uploadFile(@PartMap() LinkedHashMap<String, RequestBody> partMap, @Part List<MultipartBody.Part> names);


    @POST("/usageLog")
    Call<JSONObject> setLog(@Query("contents") String message);


    @POST("/serverPoiInfo")
    Call<ResponseBody> serverPoiInfo(@Query("areano") String areano,
                                   @Query("lineno") String lineno,
                                   @Query("stationno") String stationno,
                                   @Query("floor") String floor,
                                   @Query("userid") String userid,
                                   @Query("poi_content") String poi_content,
                                   @Query("xpos") String xpos,
                                   @Query("ypos") String ypos,
                                   @Query("poi_category") String poi_category,
                                   @Query("poi_filenm") String poi_filenm);


//    @FormUrlEncoded
//    @POST("/usageLog?contents=param")
//    Call<Data> setLog(@FieldMap HashMap<String, Object> param);

//    @FormUrlEncoded
//    @Multipart
//    @POST("/usageLog/usageLog?contents=param")
//    Call<Data> setLog(@Body String param);

    /**
     * GET 방식, URL/posts/{userId} 호출.
     * Data Type의 JSON을 통신을 통해 받음.
     * @Path("userId") String id : id 로 들어간 STring 값을, 첫 줄에 말한
     * {userId}에 넘겨줌.
     * userId에 1이 들어가면
     * "http://jsonplaceholder.typicode.com/posts/1" 이 최종 호출 주소.
     * @param userId 요청에 필요한 userId
     * @return Data 객체를 JSON 형태로 반환.
     */
    @GET("/indoormap?coordx=15551308.0000&coordy=4257982.00")
    Call<Data> getData();

    @GET("/users/{user}/repos")
    Call<Data> getMyRepos(@Path("user") String userName);

//    @GET("/posts/{userId}")
//    Call<Data> getData(@Path("userId") String userId);


    /**
     * GET 방식, URL/posts/{userId} 호출.
     * Data Type의 여러 개의 JSON을 통신을 통해 받음.
     * @Query("userId") String id : getData와 다르게 뒤에 붙는 파라미터가 없음.
     * 방식은 위와 같음.
     * 단, 주소값이 "http://jsonplaceholder.typicode.com/posts?userId=1" 이 됨.
     * @param userId 요청에 필요한 userId
     * @return 다수의 Data 객체를 JSON 형태로 반환.
     */
    @GET("indoormap?coordx=15551308.0000&coordy=4257982.00")
    Call<List<Data>> getData2();



    /**
     * POST 방식, 주소는 위들과 같음.
     * @FieldMap HashMap<String, Object> param :
     * Field 형식을 통해 넘겨주는 값들이 여러 개일 때 FieldMap을 사용함.
     * Retrofit에서는 Map 보다는 HashMap 권장.
     * @FormUrlEncoded Field 형식 사용 시 Form이 Encoding 되어야 하기 때문에 사용하는 어노테이션
     * Field 형식은 POST 방식에서만 사용가능.
     * @param param 요청에 필요한 값들.
     * @return Data 객체를 JSON 형태로 반환.
     */
    @FormUrlEncoded
    @POST("/posts")
    Call<Data> postData(@FieldMap HashMap<String, Object> param);


    /**
     * PUT 방식. 값은 위들과 같음.
     * @Body Data param : 통신을 통해 전달하는 값이 특정 JSON 형식일 경우
     * 매번 JSON 으로 변환하지 않고, 객체를 통해서 넘겨주는 방식.
     * PUT 뿐만 아니라 다른 방식에서도 사용가능.
     * @param param 전달 데이터
     * @return Data 객체를 JSON 형태로 반환.
     */
    @PUT("/posts/1")
    Call<Data> putData(@Body Data param);


    /**
     * PATCH 방식. 값은 위들과 같습니다.
     * @FIeld("title") String title : patch 방식을 통해 title 에 해당하는 값을 넘기기 위해 사용.
     * @FormUrlEncoded Field 형식 사용 시 Form이 Encoding 되어야 하기 때문에 사용하는 어노테이션
     * @param title
     * @return
     */
    @FormUrlEncoded
    @PATCH("/posts/1")
    Call<Data> patchData(@Field("title") String title);


    /**
     * DELETE 방식. 값은 위들과 같습니다.
     * Call<ResponseBody> : ResponseBody는 통신을 통해 되돌려 받는 값이 없을 경우 사용.
     * @return
     */
    @DELETE("/posts/1")
    Call<ResponseBody> deleteData();

    /*
     * DELETE 방식에서 @Body를 사용하기 위해서는 아래처럼 해야함.
     * @HTTP(method = "DELETE", path = "/Arahant/Modification/Profile/Image/User", hasBody = true)
     * Call<ResponseBody> delete(@Body RequestGet parameters);
     */

}
