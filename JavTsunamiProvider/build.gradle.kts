// use an integer for version numbers
version = 1


cloudstream {
    language = "es"
    // All of these properties are optional, you can safely remove them

    // description = "Lorem Ipsum"
    authors = listOf("RUBIKOF")
    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    /*tvTypes = listOf(
            "Anime",
            "TvSeries",
            "Movie",
    )*/
    tvTypes = listOf("NSFW")

    iconUrl = "https://www.google.com/s2/favicons?domain=javtsunami.com&sz=%size%"
}