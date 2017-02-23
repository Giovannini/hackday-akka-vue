# Akk / Vue

Une petite application pour jouer avec Akka et VueJS


## Créer un projet Akka-HTTP
Créer un projet Akka-HTTP est très simple, il suffit d'ajouter une dépendence à son `build.sbt`:
```
"com.typesafe.akka" %% "akka-http" % "10.0.3"
```
En réalité, Akka-HTTP a besoin de deux modules, `akka-http` et `akka-http-core`.
Puisque le premier dépend du second, il n'est pas nécessaire de l'importer explicitement.

### Mettre en place un serveur
Akka-HTTP présente un DSL permettant de décrire des routes HTTP et la façon de les gérer.
Chaque route est composée d'un ou plusieurs niveau de `Directives` permettant de  spécifier la gestion d'un type spécifique de requête.

```
val route =
  path("hello") { // Route gérée
    get { // Directive particulière spécifiant ne gérer que les requêtes GET
      complete( // Réponse à envoyer pour une telle directive
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          "<h1>Say hello to akka-http</h1>"
          )
        )
    }
  }
```

Les routes sont ensuite liées à un port pour démarrer un serveur HTTP:
```
Http().bindAndHandle(route, "localhost", 8080)
```
