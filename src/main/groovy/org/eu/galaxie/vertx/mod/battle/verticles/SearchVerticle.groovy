package org.eu.galaxie.vertx.mod.battle.verticles

import org.vertx.groovy.platform.Verticle


class SearchVerticle extends Verticle {
    def start() {
        /**
         * Receives search launched by Web.
         */
        vertx.eventBus.registerHandler('search') { searchMessage ->
            println "SEARCH RECEIVED"
            vertx.eventBus.send('search.remote', searchMessage.body)
            vertx.eventBus.send('search.local', searchMessage.body) { localResponse ->
                vertx.eventBus.publish('search.response', localResponse.body)
            }
        }


        vertx.eventBus.registerHandler('search.local') { searchMessage ->
            def esMessage = [
                    index: 'battle',
                    entity: 'files',
                    query: 'name:' + searchMessage.body.query
            ]
            println "Doing local search: ${esMessage.query}"

            vertx.eventBus.send('battle.elasticSearch.query', esMessage) { esResponse ->
                if (esResponse.body.body.hits) {
                    def collect = esResponse.body.body.hits.hits.collect { it.'_source' }
                    searchMessage.reply([query: searchMessage.body.query, files: collect])
                    println "found ${collect.size()} match"
                } else {
                    searchMessage.reply([query: searchMessage.body.query])
                    println "found nothing"
                }
            }
        }

        vertx.eventBus.registerHandler('search.remote') { searchMessage ->

            def xmppMessage = [
                    to: container.config.bestFriend,
                    target: 'search.local',
                    replyTo: 'search.response',
                    body: [query: searchMessage.body.query]
            ]

            vertx.eventBus.send('xmpp.send', xmppMessage)
        }


        vertx.eventBus.registerHandler('search.remote.response') { searchResponse ->
            println "My friend responded : ${searchResponse.body}"
            vertx.eventBus.send('search.remote.response.gui', searchResponse.body)
        }


        vertx.eventBus.registerHandler('getWithChunks') { searchMessage ->
            println "get with chunks"
            def esMessage = [
                    index: 'battle',
                    entity: 'files',
                    id: searchMessage.body.sha1
            ]
            println "get with chunks 2"
            vertx.eventBus.send('battle.elasticSearch.getObject', esMessage) { esResponse ->
                println "got response from ES : ${esResponse.body}"
                def fileName = esResponse.body.body.'_source'.name

                def esChunksMessage = [
                        index: 'battle',
                        entity: 'chunks',
                        query: 'belongsTo:' + searchMessage.body.sha1
                ]

                vertx.eventBus.send('battle.elasticSearch.query', esChunksMessage) { esChunksResponse ->
                    def res = esChunksResponse.body.body.hits.hits.collect { it.'_source' }.sort { it.num }.collect { it.sha1 }
                    println "COLLECTED : ${res}"
                    def landingMessage = [
                            name: fileName,
                            sha1: searchMessage.body.sha1,
                            chunks: res
                    ]
                    searchMessage.reply(landingMessage)
                }
            }
        }
    }
}
