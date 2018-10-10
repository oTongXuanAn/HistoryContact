package an.xuan.tong.historycontact.api.config

import an.xuan.tong.historycontact.api.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.math.BigDecimal
import java.util.*

open class ApiConfiguration  {
    /*internal companion object {
        fun configureMoshiBuilder(builder: Moshi.Builder): Moshi.Builder = builder
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .add(BigDecimal::class.java, BigDecimalJsonAdapter())

        fun configureRetrofitBuilder(builder: Retrofit.Builder, common: ApiConfigurationCommon,
                                     moshi: Moshi): Retrofit.Builder = builder
                .baseUrl(common.baseUrl)
                .client(common.okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
    }
    override val moshi: Moshi by lazy {
        val builder = configureMoshiBuilder(Moshi.Builder())
        builder.build()
    }

    override val retrofit: Retrofit
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mangaService: ApiService
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    protected open fun configureMoshiBuilder(builder: Moshi.Builder): Moshi.Builder {
        return Companion.configureMoshiBuilder(builder)
    }

    protected open fun configureRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return configureRetrofitBuilder(builder, common, moshi)
    }*/
}