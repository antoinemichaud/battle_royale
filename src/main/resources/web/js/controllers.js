var FileStatus = {
    SELECTED: 1,
    UPLOADING: 2,
    FINISHED: 3
};

battleModule.value('uploadFileList', []);

function RootCtrl($rootScope, $scope, $route, $routeParams, $location, eventbus, uploadFileList) {
    $scope.regions = [
        { value: "alsace", text: "Alsace" },
        { value: "aquitaine", text: "Aquitaine" },
        { value: "auvergne", text: "Auvergne" },
        { value: "basse_normandie", text: "Basse-Normandie" },
        { value: "bourgogne", text: "Bourgogne" },
        { value: "bretagne", text: "Bretagne" },
        { value: "centre", text: "Centre" },
        { value: "champagne_ardenne", text: "Champagne-Ardenne" },
        { value: "corse", text: "Corse" },
        { value: "franche_comte", text: "Franche-Comté" },
        { value: "haute_normandie", text: "Haute-Normandie" },
        { value: "ile_de_france", text: "Ile-de-France" },
        { value: "languedoc_roussillon", text: "Languedoc-Roussillon" },
        { value: "limousin", text: "Limousin" },
        { value: "lorraine", text: "Lorraine" },
        { value: "midi_pyrenees", text: "Midi-Pyrénées" },
        { value: "nord_pas_de_calais", text: "Nord-Pas-de-Calais" },
        { value: "pays_de_la_loire", text: "Pays de la Loire" },
        { value: "picardie", text: "Picardie" },
        { value: "poitou_charentes", text: "Poitou-Charentes" },
        { value: "provence_alpes_cote_d'azur", text: "Provence-Alpes-Côte d'Azur" },
        { value: "rhone_alpes", text: "Rhône-Alpes" },
        { value: "guadeloupe", text: "Guadeloupe" },
        { value: "martinique", text: "Martinique" },
        { value: "guyane", text: "Guyane" },
        { value: "reunion", text: "Réunion" }
    ];


    $scope.categories = [
        { value: "0", text: "Toutes catégories" },
        { value: "1", text: "-- VEHICULES --" },
        { value: "2", text: "Voitures" },
        { value: "3", text: "Motos" },
        { value: "4", text: "Caravaning" },
        { value: "5", text: "Utilitaires" },
        { value: "6", text: "Equipement Auto" },
        { value: "44", text: "Equipement Moto" },
        { value: "50", text: "Equipement Caravaning" },
        { value: "7", text: "Nautisme" },
        { value: "51", text: "Equipement Nautisme" },
        { value: "8", text: "-- IMMOBILIER --" },
        { value: "9", text: "Ventes immobilières" },
        { value: "10", text: "Locations" },
        { value: "11", text: "Colocations" },
        { value: "12", text: "Locations de vacances" },
        { value: "13", text: "Bureaux & Commerces" },
        { value: "14", text: "-- MULTIMEDIA --" },
        { value: "15", text: "Informatique" },
        { value: "43", text: "Consoles & Jeux vidéo" },
        { value: "16", text: "Image & Son" },
        { value: "17", text: "Téléphonie" },
        { value: "18", text: "-- MAISON --" },
        { value: "19", text: "Ameublement" },
        { value: "20", text: "Electroménager" },
        { value: "45", text: "Arts de la table" },
        { value: "39", text: "Décoration" },
        { value: "46", text: "Linge de maison" },
        { value: "21", text: "Bricolage" },
        { value: "52", text: "Jardinage" },
        { value: "22", text: "Vêtements" },
        { value: "53", text: "Chaussures" },
        { value: "47", text: "Accessoires & Bagagerie" },
        { value: "42", text: "Montres & Bijoux" },
        { value: "23", text: "Equipement bébé" },
        { value: "54", text: "Vêtements bébé" },
        { value: "24", text: "-- LOISIRS --" },
        { value: "25", text: "DVD / Films" },
        { value: "26", text: "CD / Musique" },
        { value: "27", text: "Livres" },
        { value: "28", text: "Animaux" },
        { value: "55", text: "Vélos" },
        { value: "29", text: "Sports & Hobbies" },
        { value: "30", text: "Instruments de musique" },
        { value: "40", text: "Collection" },
        { value: "41", text: "Jeux & Jouets" },
        { value: "48", text: "Vins & Gastronomie" },
        { value: "56", text: "-- MATERIEL PROFESSIONNEL --" },
        { value: "57", text: "Matériel Agricole" },
        { value: "58", text: "Transport - Manutention" },
        { value: "59", text: "BTP - Chantier Gros-oeuvre" },
        { value: "60", text: "Outillage - Matériaux 2nd-oeuvre" },
        { value: "32", text: "Équipements Industriels" },
        { value: "61", text: "Restauration - Hôtellerie" },
        { value: "62", text: "Fournitures de Bureau" },
        { value: "63", text: "Commerces & Marchés" },
        { value: "64", text: "Matériel Médical" },
        { value: "31", text: "-- EMPLOI & SERVICES --" },
        { value: "33", text: "Emploi" },
        { value: "34", text: "Services" },
        { value: "35", text: "Billetterie" },
        { value: "49", text: "Evénements" },
        { value: "36", text: "Cours particuliers" },
        { value: "37", text: "--" },
        { value: "38", text: "Autres" }
    ];
    $scope.$route = $route;
    $scope.$location = $location;
    $scope.$routeParams = $routeParams;
    $scope.FileStatus = FileStatus;

    $scope.searchResults = [];
    $scope.visibleResultIndex = null;
    $scope.availableFiles = [];

    // Upload section
    $scope.uploadFileList = uploadFileList;

    $scope.removeFile = function (index) {
        uploadFileList.splice(index, 1);
    };

    $scope.onDrop = function (file) {
        $scope.uploadFileList.push({name: file.name, status: FileStatus.SELECTED, file: file});
        $scope.$apply();
    };

    $scope.showResults = function (index) {
        $scope.visibleResultIndex = index;
        $scope.availableFiles = $scope.searchResults[index].results;
    };

    $scope.search = function (searchField) {
        $scope.searchResults.push({
            search: searchField,
            results: []
        });

        eventbus.emit('search', {query: searchField});
    };

    eventbus.on('search.response', function (remoteResponse) {
        console.log('received a message: ' + JSON.stringify(remoteResponse));

        if (null != remoteResponse.files) {
            angular.forEach($scope.searchResults, function (openSearch, index) {
                if (openSearch.search == remoteResponse.query) {
                    var sum = openSearch.results.concat(remoteResponse.files);
                    openSearch.results = sum;
                    $scope.showResults(index);
                }
            });
        }
    });
}
