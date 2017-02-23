# Akk / Vue

Une petite application pour jouer avec Akka et VueJS

### Quelques prérequis
 * Une connaissance légère de SBT
 * Une connaissance légère du protocole HTTP

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

### Jouer avec les path
Il existe de nombreuses directives permettant divers comportement de gestion des paths.
 * [`path(x)`](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/path-directives/path.html) match avec un slash (`/`) suivi par `x` puis rien d'autre.
 * [`pathPrefix(x)`](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/path-directives/pathPrefix.html) matche avec un slash (`/`) suivi par `x` puis une suite qui reste non matchée.
 * [`pathEnd(x)`](http://doc.akka.io/docs/akka-http/current/java/http/routing-dsl/directives/path-directives/pathEnd.html) matche uniquement lorsqu'il n'y a plus rien à matcher dans le path. Akka précise qu'il ne faut pas l'utilser à la racine des routes puisque le path minimal est `/`.

Exemple:
```
val route =
  pathPrefix("ball") {
    pathEnd {
      complete("/ball")
    } ~
      path(IntNumber) { int =>
        complete(if (int % 2 == 0) "even ball" else "odd ball")
      }
  }

// tests:
Get("/") ~> route ~> check {
  handled shouldEqual false
}

Get("/ball") ~> route ~> check {
  responseAs[String] shouldEqual "/ball"
}

Get("/ball/1337") ~> route ~> check {
  responseAs[String] shouldEqual "odd ball"
}
```

### La sérialisation
Dans Akka-HTTP, on appelle ça "Marshalling".
Le principe est de convertir une structure haut-niveau (un objet) en une représentation plus bas niveau.
Ici, on parle de la conversion d'un type `T` en un type plus bas niveau, par exemple le `MessageEntity` qui pourra être utilisé dans une `HttpRequest` ou une `HttpResponse`.

Pour marshaller une instance d'un type `A` en un type `B`, on utilise un `Marshaller[A, B]`.
`Marshaller[A, B]` n'est pas vraiment une fonction `A => B` mais plutôt un `Future[List[Marshalling[B]]]`.
 * `Future` car il est possible que les marshallers ne soient pas capables de produire un résultat de manière synchrone.
 * `List` puisque plutôt que d'offrir une unique représentation d'un type `A`, un marshaller peut en proposer plusieurs. C'est via l'addition d'un header `Accept` que le client pourra décider celui qui est préféré. Autrement, le premier sera choisi.
 * `Marshalling[B]` est utilisé plutôt que directement `B` pour préciser un `MediaType` et potentiellement un `HttpCharset` que le marshaller produira avant que le marshalling soit déclenché.
En plus de permettre une négociation de contenue (via l'utilisation de la liste), ce design permet de retarder la construction de l'instance cible du marshalling au dernier moment.

Il existe de [nombreux marshallers](http://doc.akka.io/docs/akka-http/current/scala/http/common/marshalling.html#predefined-marshallers) pré-définis dans Akka-HTTP.

#### Démarshaller
Le processus existe dans l'autre sens et est appelé "Unmarshalling".
Il s'agit donc de convertir une représentation bas niveau en une structure plus haut niveau.
La structure utilisée pour ce genre de conversion est un `Unmarshaller[A, B]`, qui est ici une structure très similaire à une fonction `A => Future[B]` puisqu'il n'y a pas d'intérêt à supporter une négociation de contenu et qui permet de gagner plusieurs niveaux d'indirections.

#### Le JSON
Le JSON n'est pas une des structure supportée nativement pas Akka-HTTP, il est nécessaire d'utiliser un module supplémentaire.
Celui conseillé est [spray-json](https://github.com/spray/spray-json) pour lequel un module dédié a été créé:
```
"com.typesafe.akka" %% "akka-http-spray-json" % "10.0.3"
```
Avec ça en main, nous allons pouvoir créer des format json aisément:
```
path("foo") {
  import spray.json.DefaultJsonProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  case class Foo(i: Int)
  implicit val fooFormat = jsonFormat1(Foo)
  complete(Foo(5))
}
```
