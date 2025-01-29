package rbkcloudstream


import android.annotation.TargetApi
import android.os.Build
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8
import com.lagradost.cloudstream3.utils.getQualityFromName
import com.lagradost.cloudstream3.utils.loadExtractor
import org.json.JSONObject
import java.util.Base64
import java.util.*
import kotlin.collections.ArrayList


class JavGuruProvider : MainAPI() {
    companion object {
        fun getType(t: String): TvType {
            return if (t.contains("OVA") || t.contains("Especial")) TvType.OVA
            else if (t.contains("Pelicula")) TvType.AnimeMovie
            else TvType.Anime
        }
    }

    override var mainUrl = "https://jav.guru/"
    override var name = "JavGuru    "
    override var lang = "es"
    override val hasMainPage = true
    override val hasChromecastSupport = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
            TvType.NSFW
    )
    override val mainPage = mainPageOf(
            "$mainUrl/page/" to "Main Page",
    )
    val saveImage = "";

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val urls = listOf(
                Pair(
                        "$mainUrl/tag/cuckold/",
                        "Cuckold"
                ),
                Pair(
                        "$mainUrl/tag/big-tits/",
                        "Big Tits"
                ),
                Pair(
                        "$mainUrl/tag/sister/",
                        "Sister"
                ),
                Pair(
                        "$mainUrl/tag/older-sister/",
                        "Older Sister"
                ),
                Pair(
                        "$mainUrl/tag/huge-cock/",
                        "Huge Cock"
                ),
                Pair(
                        "$mainUrl/tag/nampa/",
                        "Nampa"
                ),
                Pair(
                        "$mainUrl/tag/nurse/",
                        "Nurse"
                ),
                Pair(
                        "$mainUrl/tag/original-collaboration/",
                        "Hentai Collab"
                ),
                Pair(
                        "$mainUrl/tag/drama/",
                        "Drama"
                ),
                Pair(
                        "$mainUrl/tag/incest/",
                        "Incest"
                ),
                Pair(
                        "$mainUrl/tag/beauty-shop/",
                        "Beauty Shop"
                ),
                Pair(
                        "$mainUrl/tag/butt/",
                        "Butt"
                ),
                Pair(
                        "$mainUrl/tag/huge-butt/",
                        "Huge Butt"
                ),
        )

        val pagedLink = if (page > 0) "https://jav.guru/page/" + page else "https://jav.guru/"
        val items = ArrayList<HomePageList>()
        items.add(
                HomePageList(
                        "Recientes",
                        app.get(pagedLink).document.select("#main > div").map {
                            val title = it.selectFirst("h2")?.text()
                            val poster = it.selectFirst("img")?.attr("src")
                            val dubstat = if (title!!.contains("Latino") || title.contains("Castellano"))
                                DubStatus.Dubbed else DubStatus.Subbed
                            //val poster = it.selectFirst("a div img")?.attr("src") ?: ""

                            val url = it.selectFirst("a")?.attr("href") ?: ""


                            newAnimeSearchResponse(title, url) {
                                this.posterUrl = poster
                                addDubStatus(dubstat)
                            }
                        }, isHorizontalImages = true)
        )
        urls.apmap { (url, name) ->
            val pagedLink = if (page > 0) url + "page/" + page else url
            val soup = app.get(pagedLink).document
            val home = soup.select("#main > div").map {
                val title = it.selectFirst("h2")?.text()
                val poster = it.selectFirst("img")?.attr("src").toString()

                val link = it.selectFirst("a")?.attr("href") ?: ""

                AnimeSearchResponse(
                        title!!,
                        fixUrl(link),
                        this.name,
                        TvType.Anime,
                        fixUrl(poster),
                        null,
                        if (title.contains("Latino") || title.contains("Castellano")) EnumSet.of(
                                DubStatus.Dubbed
                        ) else EnumSet.of(DubStatus.Subbed),
                )
            }
            items.add(HomePageList(name, home, isHorizontalImages = true))
        }

        if (items.size <= 0) throw ErrorLoadingException()
        return HomePageResponse(items, hasNext = true)

    }

    data class MainSearch(
            @JsonProperty("animes") val animes: List<Animes>,
            @JsonProperty("anime_types") val animeTypes: AnimeTypes
    )

    data class Animes(
            @JsonProperty("id") val id: String,
            @JsonProperty("slug") val slug: String,
            @JsonProperty("title") val title: String,
            @JsonProperty("image") val image: String,
            @JsonProperty("synopsis") val synopsis: String,
            @JsonProperty("type") val type: String,
            @JsonProperty("status") val status: String,
            @JsonProperty("thumbnail") val thumbnail: String
    )

    data class AnimeTypes(
            @JsonProperty("TV") val TV: String,
            @JsonProperty("OVA") val OVA: String,
            @JsonProperty("Movie") val Movie: String,
            @JsonProperty("Special") val Special: String,
            @JsonProperty("ONA") val ONA: String,
            @JsonProperty("Music") val Music: String
    )

    override suspend fun search(query: String): List<SearchResponse> {

        val soup = app.get("$mainUrl//?s=$query").document
        var texto: String
        var inicio: Int
        var ultimo: Int
        var link: String
        var z: Int
        var poster = ""

            return app.get("$mainUrl//?s=$query").document
                    .select("#main .row").mapNotNull {
                        val image = it.selectFirst(".imgg img")?.attr("src").toString()
                        val title = it.selectFirst("h2 a")?.text().toString()
                        val url = fixUrlNull(it.selectFirst("h2 a")?.attr("href") ?: "") ?: return@mapNotNull null


                        MovieSearchResponse(
                                title,
                                url,
                                this.name,
                                TvType.NSFW,
                                image
                        )
        }

    }
    data class EpsInfo (
            @JsonProperty("number" ) var number : String? = null,
            @JsonProperty("title"  ) var title  : String? = null,
            @JsonProperty("image"  ) var image  : String? = null
    )
    @TargetApi(Build.VERSION_CODES.O)
    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url, timeout = 120).document
        var test ="";
        //val poster = "https://javenspanish.com/wp-content/uploads/2022/01/JUFE-132.jpg"
        val title = doc.selectFirst(".inside-article h1")?.text()?:""
        val type = "NFSW"
        val description = doc.selectFirst(".wp-content")?.text()
        val poster = doc.selectFirst(".inside-article img")?.attr("src")




        var starname = ArrayList<String>()
        var lista = ArrayList<Actor>()

        doc.select(".infoleft > ul > li").mapNotNull {
            if(it.text().contains("Actress:")){
                val names = it.text().replace("Actress:", "").trim().split(", ")

                for (name in names){
                    val r = name.split(" ")
                    val namerev = r.reversed().joinToString(" ")
                    if(namerev != ""){
                        starname.add(namerev)
                    }

                }
            }
        }
        if (starname.size>0) {

            for(i in 0 .. starname.size-1){

                app.get("https://www.javdatabase.com/idols/" + starname[i].replace(" ","-")).document.select("#main ").mapNotNull {
                    var save = it.select(".entry-content .idol-portrait img").attr("src")
                    //var otro = "https://st4.depositphotos.com/9998432/23767/v/450/depositphotos_237679112-stock-illustration-person-gray-photo-placeholder-woman.jpg"
                    var otro = "https://tse1.mm.bing.net/th?id=OIP.6_wb2dVFWij-BlgOVLAvnQAAAA&pid=15.1"
                    if(save.contains("http")){
                        lista.add(Actor(starname[i],save))
                    }else{
                        lista.add(Actor(starname[i],otro))
                    }

                }
            }
        }

            //Fin espacio prueba
        return newMovieLoadResponse(
                title,
                url,
                TvType.NSFW,
                url
        ) {
            posterUrl = fixUrlNull(poster)
            this.plot = description
            this.recommendations = null
            this.duration = null
            addActors(lista)
        }

    /* return MovieLoadResponse(
                name = title,
                url = url,
                apiName = this.name,
                type = TvType.NSFW,
                dataUrl = url,
                posterUrl = poster,
                plot = null,
                actors = lista
        )*/

    }

   /* override suspend fun loadLinks(
            data: String,
            isCasting: Boolean,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ): Boolean {
        try{
            val url = "https://ds2play.com/e/8vxqazdt0aej"
            loadExtractor(
                    url = url,
                    subtitleCallback = subtitleCallback,
                    callback = callback
            )
        } catch (e: Exception) {
            e.printStackTrace()
            logError(e)
        }
        return false
    }*/
   private fun cleanExtractor(
           source: String,
           name: String,
           url: String,
           callback: (ExtractorLink) -> Unit
   ): Boolean {
       callback(
               ExtractorLink(
                       source,
                       name,
                       url,
                       "",
                       Qualities.Unknown.value,
                       false
               )
       )
       return true
   }
   @TargetApi(Build.VERSION_CODES.O)
   override suspend fun loadLinks(
           data: String,
           isCasting: Boolean,
           subtitleCallback: (SubtitleFile) -> Unit,
           callback: (ExtractorLink) -> Unit
   ): Boolean {//https://dood.ws/e/aujxzir2eoim
       //val f = listOf("https://streamtape.net/e/4zv4vA4y9rI284/","https://streamtape.com/e/4zv4vA4y9rI284/","https://ds2play.com/e/gli2qcwpmtvl","https://v.javmix.me/vod/player.php?fl=w5qy7qg1xc6g")
       //val f = listOf("https://fastbrisk.com/e/pkbr5o5n7hvc","https://strwish.com/e/hlsubpw1u660")
       //val f = listOf("https://streamwish.top/e/k2hzsia9ltjp","https://sfastwish.com/e/ujcmxn1hw8at","https://flaswish.com/e/pkbr5o5n7hvc","https://flaswish.com/e/hlsubpw1u660")
       //val f = listOf("https://streamwish.top/e/k2hzsia9ltjp","https://flaswish.com/e/pkbr5o5n7hvc","https://streamtape.com/e/4zv4vA4y9rI284/","https://filemoon.wf/e/709h63gf9arj/PPPD-786_Seduce_al_novio_de_su_hermana_-_Yuria_Yoshine ")
       //SIII mixdrop val f = listOf("https://streamtape.net/e/4zv4vA4y9rI284/","https://mixdrop.to/e/4dkx80jmaqln1lj","https://mixdrop.ps/e/4dkx80jmaqln1lj")
       //val f = listOf("https://dood.ws/e/aujxzir2eoim","https://playerwish.com/e/k2hzsia9ltjp","https://wishfast.top/e/k2hzsia9ltjp","https://wishembed.pro/e/k2hzsia9ltjp","https://embedwish.com/e/k2hzsia9ltjp","https://dwish.pro/e/k2hzsia9ltjp","https://mwish.pro/e/k2hzsia9ltjp")
       //val f = listOf("https://streamtape.net/e/4zv4vA4y9rI284/","https://watchsb.com/v/0eo0wpztwnkz","https://ww7.embedsito.com/e/673826059907","https://streamsb.net/e/0eo0wpztwnkz","https://fastbrisk.com/e/pkbr5o5n7hvc","https://flaswish.com/e/pkbr5o5n7hvc","https://embedwish.com/e/k2hzsia9ltjp","https://mixdrop.ps/f/4dkx80jmaqln1lj","https://fastbrisk.com/e/pkbr5o5n7hvc")
       //siiii vidhidevip val f = listOf("https://dood.ws/e/aujxzir2eoim","https://vidhidevip.com/embed/lkbj6k7ipxrf","https://filemoon.wf/e/oytgjufcbzsd/SSNI-469_Stripers_de_fantasia_-_Yumi_Shion","https://moon-4uemks89-embed.com/ptsd/oytgjufcbzsd")
       //val f = listOf("https://streamtape.net/e/4zv4vA4y9rI284/","https://wishembed.pro/e/tkxnrvvcmr7d","https://emturbovid.com/t/67153f54ab258","https://emturbovid.com/t/J4GYkYjO0QdxWSERr7dJ","https://streamcdn.info/play/4KFi5bH0uv44Dx4LOJhXmb4KH0KmjM2HV8goTBKj8NNZ.html")
       //val f = listOf("https://dood.ws/e/aujxzir2eoim","https://streamwish.to/e/39vofoptz1f1","https://embedwish.com/e/82vgp2xqywzh","https://playerwish.com/e/82vgp2xqywzh","https://voe.sx/e/11qfeoizpoo7","https://brittneystandardwestern.com/e/11qfeoizpoo7")
       //val f = listOf("https://streamtape.net/e/tk9bv8ko9zgw/","https://vidhidevip.com/embed/5njbrk5z1zdh ")


       var st =""
       var dd =""
       var emt =""

       app.get(data).document.select("#wp-btn-iframe").mapNotNull {
           if(it.text().contains("STREAM ST")){
               st = it.attr("data-localize")
           }
           if(it.text().contains("STREAM DD")){
               dd = it.attr("data-localize")
           }
           if(it.text().contains("STREAM TV")){
               emt = it.attr("data-localize")
           }
       }

       var g = app.get(data).document.selectFirst("#wp-btn-iframe-js-extra").toString()
       val regex = Regex("""var\s+(\w+)\s*=\s*\{.*?"iframe_url":"([^"]+)""")
       val iframeMap = mutableMapOf<String, String>()

       regex.findAll(g).forEach { matchResult ->
           val variableName = matchResult.groups[1]?.value ?: "Unknown"
           val iframeUrl = matchResult.groups[2]?.value ?: "Unknown"
           iframeMap[variableName] = iframeUrl
           val link = String(Base64.getDecoder().decode(iframeUrl))
           val inicio = link.indexOf("=")+1
           val ultimo = link.indexOf("&bg")
           val link2 = link.substring(inicio,ultimo)
           var video = ""
           var regex : Regex
           var matchResult : MatchResult?
//aqui
           if(variableName.contains(st)){
               video = app.get("https://jav.guru/searcho/?dr="+link2.reversed()).document.selectFirst("#ideoolink")?.text().toString()
               regex = Regex("id=([^&]+)&expires")
               matchResult = regex.find(video)

               if (matchResult != null) {
                   loadExtractor("https://streamtape.net/e/" + matchResult.groupValues[1], data, subtitleCallback, callback)
               }

           }
           if(variableName.contains(emt)){
               video= app.get("https://jav.guru/searcho/?ur="+link2.reversed()).document.selectFirst("body").toString()

               regex = "var\\s+videoID\\s*=\\s*'(\\w+)'".toRegex()
               matchResult = regex.find(video)

               if (matchResult != null) {
                   loadExtractor("https://emturbovid.com/t/" + matchResult.groupValues[1],data,subtitleCallback,callback)
               }
           }

       }


       /*
       val f = listOf("https://jav.guru/searcho/?hr=713231747a736e6f76736530","https://jav.guru/searcho/?ur=742f36373433393038363165356363","https://jav.guru/searcho/?dr=656f575a4450724f41567359565761")
       f.mapNotNull{videos ->
           fetchUrls(videos).map {
               it.replace("https://dooood.com", "https://dood.ws")
                       .replace("https://dood.sh", "https://dood.ws")
                       .replace("https://dood.la","https://dood.ws")
                       .replace("https://ds2play.com","https://dood.ws")
                       .replace("https://dood.to","https://dood.ws")
           }.apmap {
               loadExtractor(it, data, subtitleCallback, callback)
               /*if(it.contains("dood")){
                   for (i in 0 .. 3){
                       if (i ==0){
                           loadExtractor(it, data, subtitleCallback, callback)
                       }
                       if(i == 1){
                           loadExtractor(it.replace("ws","la"), data, subtitleCallback, callback)
                       }
                       if(i == 2){
                           loadExtractor(it.replace("ws","sh"), data, subtitleCallback, callback)
                       }
                       if(i == 3){
                           loadExtractor(it.replace("ws","to"), data, subtitleCallback, callback)
                       }

                   }
               }*/


             /*  if (it.contains("flaswish") == true) {
                   cleanExtractor(
                           "Fireload",
                           "Fireload ",
                           it,
                           callback
                   )
               }*/


       */

       return true
   }
}