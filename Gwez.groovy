def config = [
        WebVerticle: [
                port: 8083
        ]
]

container.deployModule('org.eu.galaxie~gwez~1.0.0-final', config, 1) {
    container.logger.debug("XMPP module deployed...")
}