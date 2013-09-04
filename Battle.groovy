def config = [
        WebVerticle: [
                port: 8083
        ]
]

container.deployModule('org.eu.galaxie~battle~1.0.0-final', config, 1) {
    println "Battle royale running"
}