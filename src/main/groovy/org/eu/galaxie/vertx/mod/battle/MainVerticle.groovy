package org.eu.galaxie.vertx.mod.battle

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.impl.DefaultFutureResult

class MainVerticle extends Verticle {

    private static final String VERTICLES_PKG = 'groovy:org.eu.galaxie.vertx.mod.battle.verticles'

    def start() {
        [
                'WebVerticle',
                'SearchVerticle'
        ].each { vName ->
            def vConf = container.config[vName] ?: [:]

            container.deployVerticle("${VERTICLES_PKG}.${vName}", vConf) { DefaultFutureResult asyncRes ->

                container.logger.warn("${vName} configuration: ${vConf}")
                if (asyncRes.succeeded()) {
                    container.logger.debug("Verticle deploy ${vName}: Success")
                } else {
                    container.logger.error("Verticle deploy ${vName}: Failure", asyncRes.cause())
                }
            }
        }

        println "JSOUPING"
        Document doc = Jsoup.connect("http://www.leboncoin.fr/").get();
        Elements categories = doc.select(".CountyList a");

        println "["
        println categories.listIterator().collect {
            def readable = it.text()

            def crawlable = readable.toLowerCase()
                    .replaceAll('é', 'e')
                    .replaceAll('-', '_')
                    .replaceAll(' ', '_')
                    .replaceAll('ô', 'o')

            "{ value: \"${crawlable}\", text: \"${readable}\" }"
        }.join(',\n')
        println "]"
        println "END JSOUPING"

    }
}
