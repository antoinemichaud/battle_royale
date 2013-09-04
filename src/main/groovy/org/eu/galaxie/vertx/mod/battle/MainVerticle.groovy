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
        Document doc = Jsoup.connect("http://www.leboncoin.fr/voitures/offres").get();
        Elements categories = doc.select("#search_category option");

        println "{"
        println categories.listIterator().collect { "{ value: \"${it.attr('value')}\", text: \"${it.text()}\" }"}.join(',\n')
        println "}"
        println "END JSOUPING"

    }
}
