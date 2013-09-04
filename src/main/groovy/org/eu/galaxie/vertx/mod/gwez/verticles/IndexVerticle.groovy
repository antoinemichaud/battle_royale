package org.eu.galaxie.vertx.mod.gwez.verticles

import org.vertx.groovy.platform.Verticle

class IndexVerticle extends Verticle {

    def start() {
        vertx.eventBus.registerHandler('index') { indexMessage ->

            println "indexing : ${indexMessage.body}"

            def esMessage = [
                    index: 'gwez',
                    entity: 'files',
                    id: indexMessage.body.sha1,
                    content: [
                            name: indexMessage.body.name,
                            sha1: indexMessage.body.sha1
                    ]
            ]

            vertx.eventBus.send('gwez.elasticSearch.createObject', esMessage) { reply ->
                println "RECEIVED ${reply.body}"
            }

            int nbChunks = indexMessage.body.chunks.size()
            indexMessage.body.chunks.eachWithIndex { chunkSha1, index ->
                def esChunkMessage = [
                        index: 'gwez',
                        entity: 'chunks',
                        id: chunkSha1,
                        content: [
                                sha1: chunkSha1,
                                belongsTo: indexMessage.body.sha1,
                                num: index + 1,
                                total: nbChunks
                        ]
                ]
                println "trying to send $esChunkMessage"

                vertx.eventBus.send('gwez.elasticSearch.createObject', esChunkMessage) { reply ->
                    println "RECEIVED ${reply.body}"
                }
            }
        }
    }
}
