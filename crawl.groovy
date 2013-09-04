
/**
 * Created by amaury on 04/09/13.
 */
def pageOffre = new XmlSlurper().parseText('http://www.leboncoin.fr/annonces/offres/'.toURL().text)

pageOffre.de