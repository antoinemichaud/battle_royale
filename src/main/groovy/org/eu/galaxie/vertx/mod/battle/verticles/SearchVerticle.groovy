package org.eu.galaxie.vertx.mod.battle.verticles

import org.vertx.groovy.platform.Verticle


class SearchVerticle extends Verticle {
    def start() {
        /**
         * Receives search launched by Web.
         */
        vertx.eventBus.registerHandler('search') { searchMessage ->
            println "SEARCH RECEIVED"

        }
    }
}
