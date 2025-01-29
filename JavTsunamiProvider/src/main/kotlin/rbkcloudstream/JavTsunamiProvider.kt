package rbkcloudstream


import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8
import com.lagradost.cloudstream3.utils.getQualityFromName
import com.lagradost.cloudstream3.utils.loadExtractor
import java.util.*


class JavTsunamiProvider : MainAPI() {
    companion object {
        fun getType(t: String): TvType {
            return if (t.contains("OVA") || t.contains("Especial")) TvType.OVA
            else if (t.contains("Pelicula")) TvType.AnimeMovie
            else TvType.Anime
        }
    }

    override var mainUrl = "https://javtsunami.com/"
    override var name = "JavTsunami"
    override var lang = "es"
    override val hasMainPage = true
    override val hasChromecastSupport = true
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
            TvType.NSFW
    )
    override val mainPage = mainPageOf(
            "$mainUrl/page/1?filter=latest" to "Main Page",
    )
    val type = TvType.NSFW

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val urls = listOf(
                Pair(
                        "$mainUrl/category/featured/",
                        "Featured"
                ),
                Pair(
                        "$mainUrl/category/amateur/",
                        "Amateur"
                ),
                Pair(
                        "$mainUrl/category/milf/",
                        "Milf"
                ),


        )
        val pagedLink = if (page > 0) "$mainUrl/page/" + page + "?filter=latest" else "$mainUrl/?filter=latest"
        val items = ArrayList<HomePageList>()
        var texto: String
        var inicio: Int
        var ultimo: Int
        var link: String
        var z: Int

        items.add(
                HomePageList(
                        "Recientes",
                        app.get(pagedLink).document.select("#primary .videos-list article").map {
                            val title = it.selectFirst("header span")?.text().toString()
                            val poster = it.selectFirst(".post-thumbnail img")?.attr("data-src")
                            val url = it.selectFirst("a")?.attr("href") ?: ""


                            newAnimeSearchResponse(title, url) {
                                this.posterUrl = poster
                            }
                        },isHorizontalImages = true)
        )
        urls.apmap { (url, name) ->

            val pagedLink = if (page > 0) "$url/page/" + page else url

            val soup = app.get(pagedLink).document
            var texto: String
            var inicio: Int
            var ultimo: Int
            var link: String
            var z: Int
            var poster = ""
            val home = soup.select("#primary .videos-list article").map {
                val title = it.selectFirst("header span")?.text()
                val poster = it.selectFirst(".post-thumbnail img")?.attr("data-src").toString()
                val url = it.selectFirst("a")?.attr("href") ?: ""
                AnimeSearchResponse(
                        title!!,
                        fixUrl(url),
                        this.name,
                        TvType.NSFW,
                        fixUrl(poster),
                        null
                )
            }
            items.add(HomePageList(name, home,isHorizontalImages = true))
        }

        if (items.size <= 0) throw ErrorLoadingException()
        return HomePageResponse(items, hasNext = true)

    }

    override suspend fun search(query: String): List<SearchResponse> {

        val soup = app.get("$mainUrl//?s=$query").document

        return soup.select("#main").select("article").mapNotNull {
            val image = it.selectFirst(" div div img")?.attr("data-src")
            val title = it.selectFirst("header span")?.text().toString()
            val url = fixUrlNull(it.selectFirst("a")?.attr("href") ?: "") ?: return@mapNotNull null


            MovieSearchResponse(
                    title,
                    url,
                    this.name,
                    type,
                    image,
            )
        }

    }
    data class EpsInfo (
            @JsonProperty("number" ) var number : String? = null,
            @JsonProperty("title"  ) var title  : String? = null,
            @JsonProperty("image"  ) var image  : String? = null
    )
    override suspend fun load(url: String): LoadResponse {
        val texto: String
        var inicio: Int
        var link: String
        var poster =""
        try {
            val doc = app.get(url, timeout = 120).document
            val title = doc.selectFirst("article h1")?.text() ?: ""
            val type = "NFSW"
            texto = doc.selectFirst("#video-about .video-description figure").toString()
            inicio = texto.lastIndexOf("src") + 5
            link = texto.substring(inicio)
            poster = link.substring(0, link.indexOf("\""))
            //val poster = doc.selectFirst("#video-about .video-description img")?.attr("data-lazy-src")


            //test tmp
            var des = doc.selectFirst(".tab-content .video-description .desc").toString()
            var description = des.substring(0,des.indexOf("<figure"))


            var starname = ArrayList<String>()
            var lista = ArrayList<Actor>()

            doc.select("#video-actors a").mapNotNull {
                starname.add(it.attr("title"))
            }
            if (starname.size>0) {

                for(i in 0 .. starname.size-1){

                    var r = starname[i].split(" ")
                    app.get("https://www.javdatabase.com/idols/" + r.reversed().joinToString("-")).document.select("#main ").mapNotNull {
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

            /////Fin espacio prueba

            //parte para rellenar la lista recomendados
            val recomm = doc.select(".under-video-block .loop-video").mapNotNull {
                val href = it.selectFirst("a")!!.attr("href")
                val posterUrl = it.selectFirst("img")?.attr("data-src") ?: ""
                val name = it.selectFirst("header span")?.text() ?: ""
                MovieSearchResponse(
                        name,
                        href,
                        this.name,
                        TvType.NSFW,
                        posterUrl
                )

            }
            //finaliza la parte de relleno de recomendados
            return newMovieLoadResponse(
                    title,
                    url,
                    TvType.NSFW,
                    url
            ) {
                posterUrl = fixUrlNull(poster)
                this.plot = description
                this.recommendations = recomm
                this.duration = null
                addActors(lista)
            }
        }
        catch (e:Exception) {
            logError((e))
        }
        throw ErrorLoadingException()
        /* return MovieLoadResponse(
                 name = title,
                 url = url,
                 apiName = this.name,
                 type = TvType.NSFW,
                 dataUrl = url,
                 posterUrl = poster,
                 plot = description,
                 recommendations = recomm

         )*/

    }

    override suspend fun loadLinks(
            data: String,
            isCasting: Boolean,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ): Boolean {

        app.get(data).document.select(".entry-header .responsive-player iframe").mapNotNull{
            val videos = it.attr("src")
            fetchUrls(videos).map {
                it.replace("https://dooood.com", "https://dood.ws")
                        .replace("https://dood.sh", "https://dood.ws")
                        .replace("https://dood.la","https://dood.ws")
                        .replace("https://ds2play.com","https://dood.ws")
                        .replace("https://dood.to","https://dood.ws")
                        .replace("https://d0000d.com","https://dood.ws")

            }.apmap {
                loadExtractor(it, data, subtitleCallback, callback)
            }
        }
        return true
    }
}