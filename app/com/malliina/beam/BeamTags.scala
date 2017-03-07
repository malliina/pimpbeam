package com.malliina.beam

import com.malliina.play.tags.{Bootstrap, PlayTags, TagPage, Tags}

import scalatags.Text.all._
import controllers.routes.{ Assets => Reverse}

object BeamTags extends Tags with PlayTags with Bootstrap {
  val autoplay = attr("autoplay").empty
  val controls = attr("controls").empty
  val preload = attr("preload")

  def index = base("MusicBeamer")(
    divContainer(
      fullRow(
        headerDiv(
          h1("MusicBeamer ", small("Stream music from your mobile device to this PC."))
        )
      ),
      div(id := "initial", `class` := "row")(
        div4(
          p(id := "status", `class` := "lead")("Initializing...")
        )
      ),
      div(id := "splash", `class` := "hidden")(
        rowColumn("col-md-12 centered")(
          img(id := "qr", src := Reverse.at("img/guitar.png"), `class` := "auto-height")
        ),
        row(
          div4(
            leadPara("Get the ", strong("MusicPimp"), " app for ",
              aHref("https://play.google.com/store/apps/details?id=org.musicpimp")("Android"), ", ",
              aHref("http://www.amazon.com/gp/product/B00GVHTEJY/ref=mas_pm_musicpimp")("Kindle Fire"), ", ",
              aHref("http://www.windowsphone.com/s?appid=84cd9030-4a5c-4a03-b0ab-4d59c2fa7d42")("Windows Phone"), ", or ",
              aHref("http://apps.microsoft.com/windows/en-us/app/musicpimp/73b9a42c-e38a-4edf-ac7e-00672230f7b6")("Windows 8"), "."
            )
          ),
          div4(
            leadPara("Scan the QR code above.")
          ),
          div4(
            leadPara("Start playback from your mobile device.")
          )
        )
      ),
      div(id := "playback", `class` := "hidden")(
        rowColumn(s"$ColMd6 col-md-offset-3")(
          audio(id := "player", autoplay, preload:="none", controls)("You need to update your browser to support this feature.")
        ),
        rowColumn(s"$ColMd6 col-md-offset-3")(
          img(id :="cover", src := Reverse.at("img/guitar.png"))
        )
      ),
      jsScript(Reverse.at("js/player.js"))
    )
  )

  def base(title: String)(inner: Modifier) = TagPage(
    html(lang := En)(
      head(
        titleTag(title),
        deviceWidthViewport,
        meta(name := "description", content := "MusicBeamer lets you stream music from your mobile device to any PC. Capture the displayed image with the MusicPimp app and start playback."),
        meta(name := "keywords", content := "music,stream,musicbeamer,mp3,audio,online,musicpimp,media"),
        link(rel := "shortcut icon", href := Reverse.at("img/guitar-18x16.png")),
        link(rel := "stylesheet", href := "//netdna.bootstrapcdn.com/bootstrap/3.1.0/css/bootstrap.min.css"),
        link(rel := "stylesheet", href := Reverse.at("css/footer.css")),
        link(rel := "stylesheet", href := Reverse.at("css/player.css")),
        jsScript("//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js")
      ),
      body(
        div(id := "wrap")(
          inner,
          div(id := "push")
        ),
        div(id := "footer")(
          divContainer(
            pClass("muted credit pull-right")(
              "Inspired by ", aHref("http://www.photobeamer.com")("PhotoBeamer"),
              ". Developed by ", aHref("https://www.mskogberg.info")("Michael Skogberg"), "."
            )
          )
        )
      )
    )
  )
}
