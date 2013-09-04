package org.eu.galaxie.vertx.mod.gwez.verticles

import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.core.streams.Pump
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.file.impl.PathAdjuster
import org.vertx.java.core.impl.VertxInternal

class WebVerticle extends Verticle {

    private static final String DEFAULT_INDEX = 'index.html'
    private static final String WEB_RESOURCE_DIR = 'web'

    private static final String RESOURCE_RELATIVE_ROOT_INDEX = WEB_RESOURCE_DIR + File.separator + DEFAULT_INDEX

    private static final Map SOCKJS_CONF = [
            prefix: '/eventbus'
    ]

    private static final List SOCKJS_INBOUND_RULES = [
            [address: 'search']
    ]

    private static final List SOCKJS_OUTBOUND_RULES = [
            [address: 'search.response']
    ]

    static final Integer HTTP_SEE_OTHER = 303
    static final Integer HTTP_NOT_FOUND = 404
    static final Integer HTTP_SERVER_ERROR = 500

    private static final List NG_ENTRY_POINTS = ['/', '/upload-board', '/contact', '/search']

    Map conf = [:]

    def start() {
        conf.host = container?.config?.host ?: '0.0.0.0'
        conf.port = container?.config?.port ?: 8081

        try {
            def server = vertx.createHttpServer()

            RouteMatcher routeMatcher = buildRouteMatcher()

            server.requestHandler(routeMatcher.asClosure())

            vertx.createSockJSServer(server).bridge(SOCKJS_CONF, SOCKJS_INBOUND_RULES, SOCKJS_OUTBOUND_RULES)

            server.listen(conf.port, conf.host)

            container.logger.info("Gwez webapp on ${conf.host}:${conf.port} ...")

        } catch (Exception e) {
            container.logger.error('Startup error', e)
        }
    }

    private RouteMatcher buildRouteMatcher() {
        RouteMatcher routeMatcher = new RouteMatcher()

        routeMatcher.put('/upload', this.&routeUpload)

        routeMatcher.get('/land/:sha1', this.&routeReassemble)

        routeMatcher.noMatch(this.&routeServeFile)

        routeMatcher
    }

    private def routeUpload(HttpServerRequest req) {
        req.pause()
        // TODO : check that filename do not contains any '..'
        vertx.eventBus.send('gwez.fileYard.getBoardingPass', [:]) { boardingPass ->

            String boardingDir = boardingPass.body.directory
            String boardingName = boardingPass.body.filename

            String fileName = "${boardingDir}/${req.params.filename}"
            String tmpFileName = "${boardingDir}/${boardingName}"

            vertx.fileSystem.open(tmpFileName) { asyncRes ->
                if (asyncRes.succeeded) {
                    def file = asyncRes.result
                    def pump = Pump.createPump(req, file)

                    req.endHandler {
                        file.close {
                            vertx.fileSystem.move(tmpFileName, fileName) {

                                // File going to the warp
                                vertx.eventBus.send('gwez.fileYard.onboardFile', [filename: fileName]) { boardingResponse ->

                                    // TODO : put boardingResponse content in elastic-search for indexation

                                    def content = boardingResponse.body
                                    content.name = req.params.filename

                                    vertx.eventBus.send('index', content)
                                }
                            }
                        }
                    }

                    pump.start()
                    req.resume()
                    req.response.statusCode = 200
                    req.response.end()
                } else {
                    println "${asyncRes.cause}"
                    req.resume()
                    req.response.statusCode = HTTP_SERVER_ERROR
                    req.response.end()
                }
            }
        }
    }

    private def routeReassemble(HttpServerRequest req) {
        vertx.eventBus.send('getWithChunks', [sha1: req.params['sha1']]) { allChunksResp ->

            println "chunks description is ${allChunksResp.body}"

            vertx.eventBus.send('gwez.fileYard.landFile', allChunksResp.body) {
                println "should be reassembled as ${allChunksResp.body.name}"
                req.response.end()
            }
        }
        // get sha1, find in ES, find all chunks, launch a file.landing event
    }

    private def routeServeFile(HttpServerRequest req) {
        if (NG_ENTRY_POINTS.contains(req.path)) {
            req.response.sendFile(RESOURCE_RELATIVE_ROOT_INDEX)
        } else {
            String resourceRelativePath = WEB_RESOURCE_DIR + req.path

            File targetFileOnDisk = findOnDisk(resourceRelativePath)

            if (!resourceRelativePath.contains('..') && targetFileOnDisk.exists()) {
                if (targetFileOnDisk.isDirectory()) {
                    Boolean endsWithSlash = req.path.endsWith('/')
                    req.response.headers['Location'] = req.path + (endsWithSlash ? '' : '/') + DEFAULT_INDEX
                    req.response.statusCode = HTTP_SEE_OTHER
                    req.response.end()
                } else {
                    req.response.sendFile(resourceRelativePath)
                }
            } else {
                req.response.statusCode = HTTP_NOT_FOUND
                req.response.end()
            }
        }
    }

    private File findOnDisk(String resourceRelativePath) {

        VertxInternal core = vertx.toJavaVertx() as VertxInternal

        String pathToDisk = PathAdjuster.adjust(core, resourceRelativePath)

        new File(pathToDisk)
    }
}
