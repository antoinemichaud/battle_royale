package org.eu.galaxie.vertx.mod.gwez

import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.impl.DefaultFutureResult

class MainVerticle extends Verticle {

    private static final String VERTICLES_PKG = 'groovy:org.eu.galaxie.vertx.mod.gwez.verticles'

    def start() {

        println '   _____'
        println '  |  __ \\'
        println '  | |  \\/_      _____ ____'
        println '  | | __\\ \\ /\\ / / _ \\_  /'
        println '  | |_\\ \\\\ V  V /  __// /'
        println '   \\____/ \\_/\\_/ \\___/___|'
        println '___________________________________________________'

        container.deployModule('org.eu.galaxie~xmpp~1.0.0-final', container.config.xmpp, 1) {
            container.logger.debug("XMPP module deployed...")
        }

        [
                'WebVerticle',
                'ElasticSearchClientVerticle',
                'SearchVerticle',
                'IndexVerticle',
                'FileYardVerticle'
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
    }
}
