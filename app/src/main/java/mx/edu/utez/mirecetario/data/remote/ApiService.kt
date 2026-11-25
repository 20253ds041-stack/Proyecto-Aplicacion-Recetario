package mx.edu.utez.mirecetario.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming

/** Cliente Retrofit de la API REST local. */
interface ApiService {
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): AuthResponse
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): AuthResponse
    @POST("auth/logout") suspend fun logout()
    @GET("auth/me") suspend fun me(): UserDto
    @POST("auth/avatar") suspend fun uploadAvatar(@Body image: RequestBody): UserDto
    @DELETE("auth/avatar") suspend fun deleteAvatar(): UserDto

    @GET("recipes") suspend fun recipes(): List<RecipeDto>
    @GET("recipes/{id}") suspend fun recipe(@Path("id") id: Long): RecipeDto
    @POST("recipes") suspend fun createRecipe(@Body body: RecipeRequest): RecipeDto
    @PUT("recipes/{id}") suspend fun updateRecipe(@Path("id") id: Long, @Body body: RecipeRequest): RecipeDto
    @DELETE("recipes/{id}") suspend fun deleteRecipe(@Path("id") id: Long)

    /** Cuerpo = bytes de la imagen original; el servidor la reduce y comprime. */
    @POST("recipes/{id}/images") suspend fun uploadImage(@Path("id") id: Long, @Body image: RequestBody): PhotoDto
    @DELETE("recipes/{id}/images/{imageId}") suspend fun deleteImage(@Path("id") id: Long, @Path("imageId") imageId: Long)

    @Streaming @GET("recipes/{id}/export") suspend fun exportRecipe(@Path("id") id: Long): ResponseBody
    @POST("recipes/import") suspend fun importRecipe(@Body zip: RequestBody): RecipeDto
}
