def config = [
        WebVerticle: [
                port: 8080
        ]
]

container.deployModule('org.eu.galaxie~battle~1.0.0-final', config, 1) {
    println "Battle royale running"
}